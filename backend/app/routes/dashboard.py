"""
MedRem Backend — Dashboard Routes
GET /api/dashboard/stats
GET /api/dashboard/consistency
GET /api/dashboard/recent-reports
"""

from fastapi import APIRouter, Depends

from app.services import dashboard_service
from app.middleware.auth_middleware import get_current_user

router = APIRouter(prefix="/api/dashboard", tags=["Dashboard"])


@router.get("/stats")
async def get_dashboard_stats(current_user: dict = Depends(get_current_user)):
    """
    Get dashboard statistics:
    - Consistency score (last 30 days)
    - Active medications count
    - Perfect streak days
    - Average taken time
    - Alerts sent today
    """
    user_id = str(current_user["_id"])
    return await dashboard_service.get_dashboard_stats(user_id)


@router.get("/consistency")
async def get_today_consistency(current_user: dict = Depends(get_current_user)):
    """
    Get today's medication progress:
    - Total expected doses
    - Taken / Missed / Skipped / Upcoming counts
    - Detailed dose list with statuses
    """
    user_id = str(current_user["_id"])
    return await dashboard_service.get_today_progress(user_id)


@router.get("/recent-reports")
async def get_recent_reports(current_user: dict = Depends(get_current_user)):
    """Get the 3 most recent reports for dashboard display."""
    user_id = str(current_user["_id"])
    return await dashboard_service.get_recent_reports_for_dashboard(user_id)
