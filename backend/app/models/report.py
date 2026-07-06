"""
MedRem Backend — Report Model
MongoDB document structure for the reports collection.
"""

from datetime import datetime
from typing import Optional


def create_report_document(
    user_id: str,
    title: str,
    report_type: str,
    file_url: str,
    cloudinary_public_id: str,
    ai_summary: Optional[str] = None,
    extracted_data: Optional[dict] = None,
) -> dict:
    """
    Create a report document for MongoDB insertion.

    Args:
        user_id: The user's ObjectId as string
        title: Report title (e.g., "Blood Test Results - Oct 2024")
        report_type: Type of report (e.g., "prescription", "lab_report", "medical_record")
        file_url: Cloudinary secure URL
        cloudinary_public_id: Cloudinary public ID for deletion
        ai_summary: AI-generated summary of the report
        extracted_data: Structured data extracted by AI (e.g., medicine list)

    Returns:
        dict: Report document ready for MongoDB insert
    """
    now = datetime.utcnow()
    return {
        "user_id": user_id,
        "title": title.strip(),
        "report_type": report_type,
        "file_url": file_url,
        "cloudinary_public_id": cloudinary_public_id,
        "ai_summary": ai_summary,
        "extracted_data": extracted_data,
        "created_at": now,
        "updated_at": now,
    }
