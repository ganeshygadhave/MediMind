"""
MedRem Backend — User Schemas
Request/Response models for user profile endpoints.
"""

from pydantic import BaseModel, EmailStr
from typing import Optional


class UserProfileUpdate(BaseModel):
    """Update user profile fields (all optional)."""
    full_name: Optional[str] = None
    phone: Optional[str] = None
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    blood_type: Optional[str] = None
    allergies: Optional[list[str]] = None
    medical_conditions: Optional[list[dict]] = None
    emergency_contact: Optional[dict] = None
    profile_image_url: Optional[str] = None
    fcm_token: Optional[str] = None
    notification_settings: Optional[dict] = None


class UserProfileResponse(BaseModel):
    """User profile response."""
    id: str
    email: str
    full_name: str
    phone: Optional[str] = None
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    blood_type: Optional[str] = None
    allergies: list[str] = []
    medical_conditions: list[dict] = []
    emergency_contact: Optional[dict] = None
    profile_image_url: Optional[str] = None
    notification_settings: dict = {}
    created_at: str
    updated_at: str


class ChangePasswordRequest(BaseModel):
    """Change password request."""
    current_password: str
    new_password: str
