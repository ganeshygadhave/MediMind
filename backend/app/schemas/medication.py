"""
MedRem Backend — Medication Schemas
Request/Response models for medication endpoints.
"""

from pydantic import BaseModel, Field
from typing import Optional


class MedicationCreate(BaseModel):
    """Create a new medication entry."""
    name: str = Field(..., min_length=1, max_length=200)
    dosage: str = Field(..., min_length=1, max_length=100)
    frequency: str = Field(..., description="e.g., Once Daily, Twice Daily, As Needed (PRN)")
    reminder_times: list[str] = Field(..., description="List of HH:MM strings")
    duration_type: str = Field(default="permanent", description="permanent or custom")
    duration_start: Optional[str] = None
    duration_end: Optional[str] = None
    instructions: Optional[str] = None
    is_prn: bool = False


class MedicationUpdate(BaseModel):
    """Update a medication entry (all fields optional)."""
    name: Optional[str] = None
    dosage: Optional[str] = None
    frequency: Optional[str] = None
    reminder_times: Optional[list[str]] = None
    duration_type: Optional[str] = None
    duration_start: Optional[str] = None
    duration_end: Optional[str] = None
    instructions: Optional[str] = None
    is_prn: Optional[bool] = None
    is_active: Optional[bool] = None


class MedicationResponse(BaseModel):
    """Medication response."""
    id: str
    user_id: str
    name: str
    dosage: str
    frequency: str
    reminder_times: list[str]
    duration_type: str
    duration_start: Optional[str] = None
    duration_end: Optional[str] = None
    instructions: Optional[str] = None
    is_prn: bool
    is_active: bool
    created_at: str
    updated_at: str
