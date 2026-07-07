"""
MedRem Backend — AI Routes
POST /api/ai/chat
POST /api/ai/summarize-report
POST /api/ai/extract-medicines
"""

from fastapi import APIRouter, Depends, HTTPException, status

from app.schemas.chat import ChatRequest
from app.schemas.report import ReportSummaryRequest, ExtractMedicinesRequest
from app.services import ai_service, report_service
from app.repositories import report_repository
from app.middleware.auth_middleware import get_current_user
from app.utils.helpers import serialize_doc

router = APIRouter(prefix="/api/ai", tags=["AI Assistant"])


@router.post("/chat")
async def chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Send a message to the AI health assistant.
    The assistant has context about the user's profile, medications, and reports.
    """
    user_id = str(current_user["_id"])
    result = await ai_service.chat(
        user=current_user,
        user_id=user_id,
        message=request.message,
    )
    return result


@router.post("/summarize-report")
async def summarize_report(
    request: ReportSummaryRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Generate an AI summary of a medical report.
    Updates the report document with the generated summary.
    """
    user_id = str(current_user["_id"])

    # Get the report
    report = await report_service.get_report(request.report_id, user_id)

    # Generate summary
    summary = await ai_service.summarize_report(report["file_url"], user_id)

    # Update report with summary
    await report_repository.update_report(
        request.report_id, user_id, {"ai_summary": summary}
    )

    return {
        "report_id": request.report_id,
        "summary": summary,
    }


@router.post("/extract-medicines")
async def extract_medicines(
    request: ExtractMedicinesRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Extract medicine information from a prescription image using AI.
    Returns a list of detected medicines with dosage and frequency.
    """
    user_id = str(current_user["_id"])

    # Get the report
    report = await report_service.get_report(request.report_id, user_id)

    # Extract medicines
    medicines = await ai_service.extract_medicines(report["file_url"], user_id)

    # Update report with extracted data
    await report_repository.update_report(
        request.report_id, user_id, {"extracted_data": {"medicines": medicines}}
    )

    return {
        "report_id": request.report_id,
        "medicines": medicines,
    }
