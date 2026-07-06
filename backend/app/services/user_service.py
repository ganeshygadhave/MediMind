"""
MedRem Backend — User Service
Business logic for user profile management.
"""

from fastapi import HTTPException, status

from app.repositories import user_repository
from app.repositories import medication_repository
from app.repositories import report_repository
from app.repositories import chat_repository
from app.utils.security import hash_password, verify_password
from app.utils.helpers import serialize_doc


async def get_profile(user: dict) -> dict:
    """Get sanitized user profile."""
    user_data = serialize_doc(user)
    user_data.pop("password_hash", None)
    return user_data


async def update_profile(user_id: str, update_data: dict) -> dict:
    """
    Update user profile fields.

    Returns:
        Updated user profile
    """
    # Remove None values
    clean_data = {k: v for k, v in update_data.items() if v is not None}

    if not clean_data:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No fields to update."
        )

    await user_repository.update_user(user_id, clean_data)

    # Fetch and return updated user
    updated_user = await user_repository.find_user_by_id(user_id)
    user_data = serialize_doc(updated_user)
    user_data.pop("password_hash", None)
    return user_data


async def change_password(user: dict, current_password: str, new_password: str) -> bool:
    """
    Change user password.

    Raises:
        HTTPException 400: If current password is incorrect
    """
    if not verify_password(current_password, user["password_hash"]):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Current password is incorrect."
        )

    new_hash = hash_password(new_password)
    user_id = str(user["_id"])
    await user_repository.update_user(user_id, {"password_hash": new_hash})
    return True


async def delete_account(user_id: str) -> bool:
    """
    Delete user account and ALL associated data.
    This is a destructive, irreversible operation.
    """
    # Delete all user data from every collection
    await medication_repository.delete_all_user_medications(user_id)
    await report_repository.delete_all_user_reports(user_id)
    await chat_repository.delete_all_user_chats(user_id)
    await user_repository.delete_user(user_id)

    return True
