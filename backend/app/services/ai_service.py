"""
MedRem Backend — AI Service
Gemini 2.5 Flash integration for chat, report summarization, and medicine extraction.
"""

import json
import google.generativeai as genai
from fastapi import HTTPException, status

from app.config import settings
from app.models.chat import create_chat_message_document
from app.repositories import chat_repository, medication_repository, report_repository
from app.utils.helpers import serialize_doc

# ── System Prompt ────────────────────────────────────────

SYSTEM_PROMPT = """You are MedRem Assistant, an AI health assistant integrated into the MedRem medication management app. 

CORE RULES (NEVER VIOLATE):
1. You INFORM, SUMMARIZE, and ASSIST — you NEVER diagnose medical conditions.
2. You NEVER prescribe medication or recommend specific drugs.
3. You NEVER recommend stopping or changing medication dosages.
4. You NEVER replace healthcare professionals — always recommend consulting a doctor for medical decisions.
5. You maintain a CALM, SAFE, and SUPPORTIVE tone at all times.
6. If a user describes a medical emergency, advise them to call emergency services immediately.

CAPABILITIES:
- Answer questions about the user's current medications (names, dosages, schedules)
- Provide general health information and wellness tips
- Help users understand their medication schedules
- Summarize medication adherence data
- Explain common medical terms in simple language
- Provide encouragement for medication adherence

CONTEXT: You have access to the user's profile, medications, and report summaries. Use this context to give personalized, relevant responses.

Always be concise but thorough. Use bullet points and structured formatting when listing information."""

REPORT_SUMMARY_PROMPT = """Analyze this medical report and provide a clear, structured summary.

Include:
1. **Report Type**: What kind of report this is
2. **Key Findings**: Main results or observations
3. **Notable Values**: Any values outside normal ranges (if applicable)
4. **Summary**: A brief, patient-friendly summary

Keep the language simple and accessible. Do NOT diagnose or recommend treatment.
If you cannot read or understand the report, say so clearly."""

MEDICINE_EXTRACTION_PROMPT = """Extract all medication information from this prescription image.

For each medication found, provide:
- **name**: Medicine name
- **dosage**: Dosage amount (e.g., "500mg", "10mg")
- **frequency**: How often to take it (e.g., "Once Daily", "Twice Daily", "Three Times Daily")
- **instructions**: Any special instructions (e.g., "Take with food", "Before bedtime")

Return the result as a JSON array of objects with keys: name, dosage, frequency, instructions.
If you cannot read the prescription clearly, indicate which parts are unclear.
Do NOT guess or infer medications that aren't clearly written."""


def _init_gemini():
    """Initialize Gemini with API key."""
    genai.configure(api_key=settings.GEMINI_API_KEY)


def _get_model():
    """Get the Gemini model instance."""
    _init_gemini()
    return genai.GenerativeModel("gemini-2.5-flash")


async def _build_user_context(user: dict, user_id: str) -> str:
    """Build context string from user's profile, medications, and reports."""
    context_parts = []

    # User profile
    name = user.get("full_name", "User")
    context_parts.append(f"Patient Name: {name}")

    if user.get("blood_type"):
        context_parts.append(f"Blood Type: {user['blood_type']}")

    if user.get("allergies"):
        context_parts.append(f"Known Allergies: {', '.join(user['allergies'])}")

    if user.get("medical_conditions"):
        conditions = [c.get("name", "") for c in user["medical_conditions"] if c.get("name")]
        if conditions:
            context_parts.append(f"Medical Conditions: {', '.join(conditions)}")

    # Current medications
    medications = await medication_repository.find_medications_by_user(user_id, active_only=True)
    if medications:
        context_parts.append("\nCurrent Medications:")
        for med in medications:
            times = ", ".join(med.get("reminder_times", []))
            context_parts.append(
                f"  - {med['name']} ({med['dosage']}) - {med['frequency']}, Times: {times}"
            )

    # Recent report summaries
    reports = await report_repository.find_recent_reports(user_id, limit=5)
    if reports:
        context_parts.append("\nRecent Medical Reports:")
        for report in reports:
            title = report.get('title', 'Untitled')
            context_parts.append(f"  - Title: {title}")
            
            # Add AI Summary if available
            if report.get('ai_summary'):
                context_parts.append(f"    Summary: {report['ai_summary']}")
                
            # Add Extracted Data if available (e.g. medicines)
            extracted_data = report.get('extracted_data', {})
            if extracted_data and 'medicines' in extracted_data:
                meds_str = ", ".join([med.get('name', 'Unknown') for med in extracted_data['medicines']])
                context_parts.append(f"    Extracted Medicines: {meds_str}")
                
    return "\n".join(context_parts)


async def chat(user: dict, user_id: str, message: str) -> dict:
    """
    Process a chat message with the agentic AI health assistant.
    The agent autonomously decides which tools to call (meds, profile, reports).
    """
    try:
        from app.services.health_agent import run_health_agent
        
        # 1. Run the agentic loop
        agent_response = await run_health_agent(user_id, message)
        answer = agent_response.get("answer", "")
        
        # 2. Save user message to history
        user_msg_doc = create_chat_message_document(
            user_id=user_id, role="user", content=message
        )
        await chat_repository.save_message(user_msg_doc)

        # 3. Save assistant response to history
        metadata = {
            "warning_level": agent_response.get("warning_level"),
            "sources_used": agent_response.get("sources_used"),
            "suggested_actions": agent_response.get("suggested_actions")
        }
        assistant_msg_doc = create_chat_message_document(
            user_id=user_id, 
            role="assistant", 
            content=answer,
            metadata=metadata
        )
        await chat_repository.save_message(assistant_msg_doc)

        return serialize_doc(assistant_msg_doc)

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"AI agent error: {str(e)}"
        )


async def summarize_report(report_url: str, user_id: str) -> str:
    """
    Summarize a medical report using Gemini vision/text capabilities.

    Args:
        report_url: Cloudinary URL of the report
        user_id: The user's ID

    Returns:
        AI-generated summary string
    """
    try:
        model = _get_model()

        prompt = f"""{REPORT_SUMMARY_PROMPT}

Report URL: {report_url}

Please analyze and summarize this medical report:"""

        response = model.generate_content(prompt)
        return response.text

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Report summarization failed: {str(e)}"
        )


async def extract_medicines(report_url: str, user_id: str) -> list[dict]:
    """
    Extract medicine information from a prescription image using a LangGraph Agent.
    The agent cross-references with user history for duplicates and interaction warnings.
    """
    try:
        from app.services.health_agent import run_extraction_agent
        
        # Run the agentic extraction loop
        result = await run_extraction_agent(user_id, report_url)
        
        # Return the list of medicines (the route expects a list[dict])
        return result.get("medicines", [])

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Medicine extraction agent failed: {str(e)}"
        )
