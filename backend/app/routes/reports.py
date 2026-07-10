"""
MedRem Backend — Report Routes
POST   /api/reports/upload
GET    /api/reports
GET    /api/reports/{id}
DELETE /api/reports/{id}
PATCH  /api/reports/{id}/rename
POST   /api/reports/next-title
"""

from fastapi import APIRouter, Depends, File, Form, UploadFile

from app.schemas.auth import MessageResponse
from app.schemas.report import RenameReportRequest, AutoTitleRequest, AutoTitleResponse
from app.services import report_service
from app.middleware.auth_middleware import get_current_user

router = APIRouter(prefix="/api/reports", tags=["Reports"])


@router.post("/upload")
async def upload_report(
    file: UploadFile = File(...),
    title: str = Form(...),
    report_type: str = Form(default="medical_record"),
    current_user: dict = Depends(get_current_user),
):
    """
    Upload a medical report or prescription.
    File is stored in Cloudinary, metadata in MongoDB.
    """
    user_id = str(current_user["_id"])
    return await report_service.upload_report(
        user_id=user_id,
        file=file,
        title=title,
        report_type=report_type,
    )


@router.get("")
async def get_all_reports(current_user: dict = Depends(get_current_user)):
    """Get all reports for the current user."""
    user_id = str(current_user["_id"])
    return await report_service.get_all_reports(user_id)


@router.get("/{report_id}")
async def get_report(
    report_id: str,
    current_user: dict = Depends(get_current_user),
):
    """Get a specific report by ID with AI summary."""
    user_id = str(current_user["_id"])
    return await report_service.get_report(report_id, user_id)


@router.delete("/{report_id}", response_model=MessageResponse)
async def delete_report(
    report_id: str,
    current_user: dict = Depends(get_current_user),
):
    """Delete a report (removes from Cloudinary and MongoDB)."""
    user_id = str(current_user["_id"])
    await report_service.delete_report(report_id, user_id)
    return {"message": "Report deleted successfully."}


@router.patch("/{report_id}/rename")
async def rename_report(
    report_id: str,
    request: RenameReportRequest,
    current_user: dict = Depends(get_current_user),
):
    """Rename a report's title."""
    user_id = str(current_user["_id"])
    return await report_service.rename_report(report_id, user_id, request.title)


@router.post("/next-title", response_model=AutoTitleResponse)
async def get_next_title(
    request: AutoTitleRequest,
    current_user: dict = Depends(get_current_user),
):
    """Get the next auto-generated title for the given upload source."""
    user_id = str(current_user["_id"])
    title = await report_service.generate_auto_title(user_id, request.source)
    return {"title": title}
