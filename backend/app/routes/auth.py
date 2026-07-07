"""
MedRem Backend — Auth Routes
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
"""

from fastapi import APIRouter, Depends

from app.schemas.auth import RegisterRequest, LoginRequest, AuthResponse, MessageResponse
from app.services import auth_service
from app.middleware.auth_middleware import get_current_user
from app.utils.helpers import serialize_doc

router = APIRouter(prefix="/api/auth", tags=["Authentication"])


@router.post("/register", response_model=AuthResponse)
async def register(request: RegisterRequest):
    """Register a new user account with optional health profile."""
    result = await auth_service.register_user(
        email=request.email,
        password=request.password,
        full_name=request.full_name,
        phone=request.phone,
        date_of_birth=request.date_of_birth,
        gender=request.gender,
        blood_type=request.blood_type,
        allergies=request.allergies,
        medical_conditions=request.medical_conditions,
        emergency_contact=request.emergency_contact,
        fcm_token=request.fcm_token,
    )
    return result


@router.post("/login", response_model=AuthResponse)
async def login(request: LoginRequest):
    """Login with email and password. Returns JWT access token."""
    result = await auth_service.login_user(
        email=request.email,
        password=request.password,
        fcm_token=request.fcm_token,
    )
    return result


@router.post("/logout", response_model=MessageResponse)
async def logout(current_user: dict = Depends(get_current_user)):
    """Logout (client should discard the token)."""
    return {"message": "Successfully logged out."}


@router.get("/me")
async def get_me(current_user: dict = Depends(get_current_user)):
    """Get the current authenticated user's profile."""
    profile = await auth_service.get_current_user_profile(current_user)
    return profile
