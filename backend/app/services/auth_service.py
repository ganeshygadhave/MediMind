"""
MedRem Backend — Auth Service
Business logic for user registration and login.
"""

from fastapi import HTTPException, status

from app.models.user import create_user_document
from app.repositories import user_repository
from app.utils.security import hash_password, verify_password, create_access_token
from app.utils.helpers import serialize_doc


async def register_user(
    email: str,
    password: str,
    full_name: str,
    **kwargs
) -> dict:
    """
    Register a new user.

    Raises:
        HTTPException 409: If email already exists
    """
    # Check if email already exists
    existing = await user_repository.find_user_by_email(email)
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this email already exists."
        )

    # Hash the password
    password_hash = hash_password(password)

    # Create user document
    user_doc = create_user_document(
        email=email,
        password_hash=password_hash,
        full_name=full_name,
        **kwargs
    )

    # Insert into database
    user_id = await user_repository.create_user(user_doc)

    # Generate JWT token
    token = create_access_token(data={"sub": user_id})

    # Return user data (excluding password)
    user_doc["_id"] = user_id
    user_data = serialize_doc(user_doc)
    user_data.pop("password_hash", None)

    return {
        "access_token": token,
        "token_type": "bearer",
        "user": user_data
    }


async def login_user(email: str, password: str) -> dict:
    """
    Authenticate a user with email and password.

    Raises:
        HTTPException 401: If credentials are invalid
    """
    # Find user
    user = await user_repository.find_user_by_email(email)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password."
        )

    # Verify password
    if not verify_password(password, user["password_hash"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password."
        )

    # Generate JWT token
    user_id = str(user["_id"])
    token = create_access_token(data={"sub": user_id})

    # Return user data (excluding password)
    user_data = serialize_doc(user)
    user_data.pop("password_hash", None)

    return {
        "access_token": token,
        "token_type": "bearer",
        "user": user_data
    }


async def get_current_user_profile(user: dict) -> dict:
    """Get sanitized user profile from the authenticated user dict."""
    user_data = serialize_doc(user)
    user_data.pop("password_hash", None)
    return user_data
