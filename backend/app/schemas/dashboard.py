"""
MedRem Backend — Dashboard Schemas
Response models for dashboard endpoints.
"""

from pydantic import BaseModel
from typing import Optional


class DashboardStats(BaseModel):
    """Dashboard statistics response."""
    consistency_score: float  # 0-100 percentage
    total_medications: int
    active_medications: int
    perfect_streak_days: int
    avg_taken_time: Optional[str] = None  # HH:MM AM/PM
    alerts_sent_today: int


class TodayProgress(BaseModel):
    """Today's medication progress."""
    total_doses: int
    taken: int
    missed: int
    skipped: int
    upcoming: int
    doses: list[dict]  # List of dose details


class StatusBreakdown(BaseModel):
    """Status breakdown for the consistency chart."""
    taken: int
    missed: int
    skipped: int


class UpcomingMedication(BaseModel):
    """Upcoming medication dose."""
    medication_id: str
    name: str
    dosage: str
    time: str
    status: str  # "upcoming", "taken", "missed", "skipped"


class RecentReport(BaseModel):
    """Recent report summary for dashboard."""
    id: str
    title: str
    subtitle: str
    report_type: str
    created_at: str


class DoseLogRequest(BaseModel):
    """Log a dose action (taken, skipped, remind_later)."""
    medication_id: str
    scheduled_time: str  # HH:MM
    action: str  # "taken", "skipped", "remind_later"
    notes: Optional[str] = None
