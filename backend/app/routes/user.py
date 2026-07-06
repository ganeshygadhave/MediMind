"""
MedRem Backend — User Routes
GET    /api/user/profile
PUT    /api/user/profile
PUT    /api/user/password
DELETE /api/user/account
"""

from fastapi import APIRouter, Depends

from app.schemas.user import UserProfileUpdate, ChangePasswordRequest
from app.schemas.auth import MessageResponse
from app.services import user_service
from app.middleware.auth_middleware import get_current_user

router = APIRouter(prefix="/api/user", tags=["User Profile"])


@router.get("/profile")
async def get_profile(current_user: dict = Depends(get_current_user)):
    """Get the current user's full profile."""
    return await user_service.get_profile(current_user)


@router.put("/profile")
async def update_profile(
    request: UserProfileUpdate,
    current_user: dict = Depends(get_current_user),
):
    """Update the current user's profile fields."""
    user_id = str(current_user["_id"])
    update_data = request.model_dump(exclude_unset=True)
    return await user_service.update_profile(user_id, update_data)


@router.put("/password", response_model=MessageResponse)
async def change_password(
    request: ChangePasswordRequest,
    current_user: dict = Depends(get_current_user),
):
    """Change the current user's password."""
    await user_service.change_password(
        user=current_user,
        current_password=request.current_password,
        new_password=request.new_password,
    )
    return {"message": "Password changed successfully."}


@router.delete("/account", response_model=MessageResponse)
async def delete_account(current_user: dict = Depends(get_current_user)):
    """
    Permanently delete the current user's account and ALL associated data.
    This action is irreversible.
    """
    user_id = str(current_user["_id"])
    await user_service.delete_account(user_id)
    return {"message": "Account and all associated data have been permanently deleted."}
