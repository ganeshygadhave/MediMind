"""
MedRem Backend — Report Service
Business logic for report upload, retrieval, deletion, rename, and auto-naming.
"""

from fastapi import HTTPException, UploadFile, status

from app.models.report import create_report_document
from app.repositories import report_repository
from app.services import cloudinary_service
from app.utils.helpers import serialize_doc


async def generate_auto_title(user_id: str, source: str) -> str:
    """
    Generate an auto-incrementing title based on upload source.
    source: 'prescription' | 'report' | 'chatbot'
    """
    prefix_map = {
        "prescription": "Prescription",
        "report": "Report",
        "chatbot": "Chatbot File",
    }
    prefix = prefix_map.get(source, "Report")
    count = await report_repository.count_by_prefix(user_id, prefix)
    return f"{prefix} {count + 1}"


async def upload_report(
    user_id: str,
    file: UploadFile,
    title: str,
    report_type: str = "medical_record",
) -> dict:
    """
    Upload a report: store file in Cloudinary, save metadata to MongoDB.
    """
    # Upload file to Cloudinary
    upload_result = await cloudinary_service.upload_file(file, folder="medrem/reports")

    # Create report document
    report_doc = create_report_document(
        user_id=user_id,
        title=title,
        report_type=report_type,
        file_url=upload_result["secure_url"],
        cloudinary_public_id=upload_result["public_id"],
    )

    report_id = await report_repository.create_report(report_doc)
    report_doc["_id"] = report_id
    return serialize_doc(report_doc)


async def get_all_reports(user_id: str) -> list[dict]:
    """Get all reports for the user."""
    reports = await report_repository.find_reports_by_user(user_id)
    return [serialize_doc(report) for report in reports]


async def get_report(report_id: str, user_id: str) -> dict:
    """
    Get a specific report by ID.

    Raises:
        HTTPException 404: If report not found
    """
    report = await report_repository.find_report_by_id(report_id, user_id)
    if not report:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Report not found."
        )
    return serialize_doc(report)


async def delete_report(report_id: str, user_id: str) -> bool:
    """
    Delete a report: remove from Cloudinary and MongoDB.

    Raises:
        HTTPException 404: If report not found
    """
    # Get report to find Cloudinary public_id
    report = await report_repository.find_report_by_id(report_id, user_id)
    if not report:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Report not found."
        )

    # Delete from Cloudinary (best effort)
    try:
        await cloudinary_service.delete_file(report["cloudinary_public_id"])
    except Exception:
        pass  # Continue even if Cloudinary delete fails

    # Delete from MongoDB
    await report_repository.delete_report(report_id, user_id)
    return True


async def rename_report(report_id: str, user_id: str, new_title: str) -> dict:
    """
    Rename a report's title.

    Raises:
        HTTPException 404: If report not found
        HTTPException 400: If title is empty
    """
    if not new_title or not new_title.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title cannot be empty."
        )

    report = await report_repository.find_report_by_id(report_id, user_id)
    if not report:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Report not found."
        )

    await report_repository.rename_report(report_id, user_id, new_title)
    # Fetch updated
    updated = await report_repository.find_report_by_id(report_id, user_id)
    return serialize_doc(updated)
