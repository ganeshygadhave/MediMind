"""
MedRem Backend — Auth Schemas
Request/Response models for authentication endpoints.
"""

from pydantic import BaseModel, EmailStr, Field
from typing import Optional


# ── Request Schemas ──────────────────────────────────────


class RegisterRequest(BaseModel):
    """Registration request with account info and optional health profile."""
    email: EmailStr
    password: str = Field(..., min_length=8, max_length=128)
    full_name: str = Field(..., min_length=2, max_length=100)
    phone: Optional[str] = None
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    blood_type: Optional[str] = None
    allergies: Optional[list[str]] = None
    medical_conditions: Optional[list[dict]] = None
    emergency_contact: Optional[dict] = None
    fcm_token: Optional[str] = None


class LoginRequest(BaseModel):
    """Login request with email and password."""
    email: EmailStr
    password: str
    fcm_token: Optional[str] = None


# ── Response Schemas ─────────────────────────────────────


class AuthResponse(BaseModel):
    """Authentication response with JWT token and user info."""
    access_token: str
    token_type: str = "bearer"
    user: dict


class MessageResponse(BaseModel):
    """Generic message response."""
    message: str
