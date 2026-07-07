"""
MedRem Backend — AI Service
Gemini 2.5 Flash integration for chat, report summarization, and medicine extraction.
"""

import json
import os
from langchain_groq import ChatGroq
from langchain_core.messages import HumanMessage
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

MEDICINE_EXTRACTION_PROMPT = """You are a medical data extraction expert. 
Your task is to scan the entire prescription image and extract EVERY SINGLE medication mentioned. 

For EACH medication found, provide:
- **name**: Exact medicine name
- **dosage**: Dosage amount (e.g., "500mg", "10mg")
- **frequency**: How often to take it (e.g., "Once Daily", "Twice Daily")
- **instructions**: Special instructions (e.g., "Take with food")

OUTPUT RULES:
1. You MUST return a JSON array of objects.
2. If there are 10 medicines, return all 10. Do NOT stop after the first one.
3. Return ONLY the JSON array. No conversational text.
4. If a value is unknown, use "Not specified"."""



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

        # 4. Return flattened response to match Android DTO
        return {
            "role": "assistant",
            "answer": answer,
            "warning_level": agent_response.get("warning_level", "none"),
            "sources_used": agent_response.get("sources_used", []),
            "suggested_actions": agent_response.get("suggested_actions", []),
            "timestamp": assistant_msg_doc["timestamp"].isoformat()
        }

    except Exception as e:
        print(f"DEBUG: Chat Error: {str(e)}")
        import traceback
        traceback.print_exc()
        # Return structured error to avoid Android app crash
        return {
            "role": "assistant",
            "answer": f"Sorry, I encountered an error: {str(e)}",
            "warning_level": "medium",
            "sources_used": [],
            "suggested_actions": ["Check API Key", "Retry later"]
        }


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
        import httpx
        import base64
        import io
        from pypdf import PdfReader

        # 1. Download the file
        async with httpx.AsyncClient() as client:
            resp = await client.get(report_url)
            if resp.status_code != 200:
                print(f"DEBUG: Download failed for {report_url} with status {resp.status_code}")
                return "Unauthorized: Please ensure your Cloudinary reports folder is set to 'Public' (not 'Private')."
            file_bytes = resp.content
            content_type = resp.headers.get("Content-Type", "")

        # 2. Case A: It's a PDF -> Extract text and summarize
        if "pdf" in content_type.lower() or report_url.lower().endswith(".pdf"):
            pdf_file = io.BytesIO(file_bytes)
            reader = PdfReader(pdf_file)
            extracted_text = ""
            for page in reader.pages:
                extracted_text += page.extract_text() + "\n"
            
            if len(extracted_text.strip()) < 10:
                return "The PDF seems to be an image/scan. Please upload a JPEG or PNG of the report instead."

            if settings.GROQ_API_KEY:
                groq_llm = ChatGroq(model="llama-3.3-70b-versatile", groq_api_key=settings.GROQ_API_KEY)
                prompt = f"{REPORT_SUMMARY_PROMPT}\n\nPDF TEXT:\n{extracted_text}"
                response = await groq_llm.ainvoke([HumanMessage(content=prompt)])
                return response.content
            return "Groq key missing."

        # 3. Case B: It's an Image -> Use Vision
        if settings.GROQ_API_KEY:
            image_b64 = base64.b64encode(file_bytes).decode("utf-8")
            groq_llm = ChatGroq(model="meta-llama/llama-4-scout-17b-16e-instruct", groq_api_key=settings.GROQ_API_KEY)
            message = HumanMessage(content=[
                {"type": "text", "text": f"{REPORT_SUMMARY_PROMPT}\nAnalyze this report."},
                {"type": "image_url", "image_url": {"url": f"data:{content_type};base64,{image_b64}"}}
            ])
            response = await groq_llm.ainvoke([message])
            return response.content
        
        return "Groq API Key is missing."

    except Exception as e:
        print(f"DEBUG: Summarization Error: {str(e)}")
        import traceback
        traceback.print_exc()
        return f"Summarization failed: {str(e)}"


async def extract_medicines(report_url: str, user_id: str) -> list[dict]:
    """
    Extract medicine information from a prescription image or PDF using Groq.
    """
    try:
        import httpx
        import base64
        import io
        from pypdf import PdfReader

        if settings.GROQ_API_KEY:
            # 1. Download file
            async with httpx.AsyncClient() as client:
                resp = await client.get(report_url)
                if resp.status_code != 200:
                    return []
                file_bytes = resp.content
                content_type = resp.headers.get("Content-Type", "")

            # 2. Case A: PDF Extraction
            if "pdf" in content_type.lower() or report_url.lower().endswith(".pdf"):
                pdf_file = io.BytesIO(file_bytes)
                reader = PdfReader(pdf_file)
                extracted_text = ""
                for page in reader.pages:
                    extracted_text += page.extract_text() + "\n"
                
                groq_llm = ChatGroq(model="llama-3.3-70b-versatile", groq_api_key=settings.GROQ_API_KEY)
                prompt = f"{MEDICINE_EXTRACTION_PROMPT}\n\nPDF TEXT CONTENT:\n{extracted_text}"
                response = await groq_llm.ainvoke([HumanMessage(content=prompt)])
            
            # 3. Case B: Image Extraction
            else:
                image_b64 = base64.b64encode(file_bytes).decode("utf-8")
                groq_llm = ChatGroq(model="meta-llama/llama-4-scout-17b-16e-instruct", groq_api_key=settings.GROQ_API_KEY)
                message = HumanMessage(content=[
                    {"type": "text", "text": f"{MEDICINE_EXTRACTION_PROMPT}\nExtract medications from this document."},
                    {"type": "image_url", "image_url": {"url": f"data:{content_type};base64,{image_b64}"}}
                ])
                response = await groq_llm.ainvoke([message])

            # Parse the JSON from Groq's response
            try:
                content = response.content
                if "[" in content and "]" in content:
                    json_str = content[content.find("["):content.rfind("]")+1]
                    return json.loads(json_str)
                return []
            except:
                return []
        
        return []

    except Exception as e:
        print(f"DEBUG: Extraction Error: {str(e)}")
        return []
