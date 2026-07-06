"""
MedRem Backend — Reminder Routes
POST /api/reminders/taken
POST /api/reminders/skipped
POST /api/reminders/remind-later
"""

from fastapi import APIRouter, Depends

from app.schemas.dashboard import DoseLogRequest
from app.services import dashboard_service
from app.middleware.auth_middleware import get_current_user

router = APIRouter(prefix="/api/reminders", tags=["Reminders"])


@router.post("/taken")
async def mark_taken(
    request: DoseLogRequest,
    current_user: dict = Depends(get_current_user),
):
    """Mark a medication dose as taken."""
    user_id = str(current_user["_id"])
    result = await dashboard_service.log_dose_action(
        user_id=user_id,
        medication_id=request.medication_id,
        scheduled_time=request.scheduled_time,
        action="taken",
        notes=request.notes,
    )
    return result


@router.post("/skipped")
async def mark_skipped(
    request: DoseLogRequest,
    current_user: dict = Depends(get_current_user),
):
    """Mark a medication dose as skipped."""
    user_id = str(current_user["_id"])
    result = await dashboard_service.log_dose_action(
        user_id=user_id,
        medication_id=request.medication_id,
        scheduled_time=request.scheduled_time,
        action="skipped",
        notes=request.notes,
    )
    return result


@router.post("/remind-later")
async def remind_later(
    request: DoseLogRequest,
    current_user: dict = Depends(get_current_user),
):
    """Snooze a medication reminder for later."""
    user_id = str(current_user["_id"])
    result = await dashboard_service.log_dose_action(
        user_id=user_id,
        medication_id=request.medication_id,
        scheduled_time=request.scheduled_time,
        action="remind_later",
        notes=request.notes,
    )
    return result
