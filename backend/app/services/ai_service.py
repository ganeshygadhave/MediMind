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
    reports = await report_repository.find_recent_reports(user_id, limit=3)
    if reports:
        context_parts.append("\nRecent Reports:")
        for report in reports:
            context_parts.append(f"  - {report.get('title', 'Untitled')}")

    return "\n".join(context_parts)


async def chat(user: dict, user_id: str, message: str) -> dict:
    """
    Process a chat message with the AI assistant.
    Injects user context and chat history into the prompt.
    """
    try:
        model = _get_model()

        # Build context
        user_context = await _build_user_context(user, user_id)

        # Get recent chat history
        recent_messages = await chat_repository.get_recent_messages(user_id, limit=10)
        history_text = ""
        for msg in recent_messages:
            role = "User" if msg["role"] == "user" else "Assistant"
            history_text += f"{role}: {msg['content']}\n"

        # Build the full prompt
        full_prompt = f"""{SYSTEM_PROMPT}

--- PATIENT CONTEXT ---
{user_context}

--- CONVERSATION HISTORY ---
{history_text}

--- CURRENT MESSAGE ---
User: {message}

Please respond helpfully and safely:"""

        # Generate response
        response = model.generate_content(full_prompt)
        assistant_message = response.text

        # Save user message
        user_msg_doc = create_chat_message_document(
            user_id=user_id, role="user", content=message
        )
        await chat_repository.save_message(user_msg_doc)

        # Save assistant response
        assistant_msg_doc = create_chat_message_document(
            user_id=user_id, role="assistant", content=assistant_message
        )
        await chat_repository.save_message(assistant_msg_doc)

        return serialize_doc(assistant_msg_doc)

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"AI service error: {str(e)}"
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


async def extract_medicines(report_url: str) -> list[dict]:
    """
    Extract medicine information from a prescription image.

    Args:
        report_url: Cloudinary URL of the prescription

    Returns:
        List of extracted medicines with name, dosage, frequency, instructions
    """
    try:
        model = _get_model()

        prompt = f"""{MEDICINE_EXTRACTION_PROMPT}

Prescription URL: {report_url}

Extract all medications and return as JSON array:"""

        response = model.generate_content(prompt)
        response_text = response.text

        # Try to parse JSON from the response
        try:
            # Strip markdown code block if present
            clean_text = response_text.strip()
            if clean_text.startswith("```json"):
                clean_text = clean_text[7:]
            if clean_text.startswith("```"):
                clean_text = clean_text[3:]
            if clean_text.endswith("```"):
                clean_text = clean_text[:-3]

            medicines = json.loads(clean_text.strip())
            if isinstance(medicines, list):
                return medicines
            return []
        except json.JSONDecodeError:
            # Return raw text as single item if JSON parsing fails
            return [{"raw_response": response_text}]

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Medicine extraction failed: {str(e)}"
        )
