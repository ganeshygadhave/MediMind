"""
MedRem Backend — User Model
MongoDB document structure for the users collection.
"""

from datetime import datetime
from typing import Optional


def create_user_document(
    email: str,
    password_hash: str,
    full_name: str,
    phone: Optional[str] = None,
    date_of_birth: Optional[str] = None,
    gender: Optional[str] = None,
    blood_type: Optional[str] = None,
    allergies: Optional[list[str]] = None,
    medical_conditions: Optional[list[dict]] = None,
    emergency_contact: Optional[dict] = None,
    fcm_token: Optional[str] = None,
) -> dict:
    """
    Create a user document for MongoDB insertion.

    Returns:
        dict: User document ready for MongoDB insert
    """
    now = datetime.utcnow()
    return {
        "email": email.lower().strip(),
        "password_hash": password_hash,
        "full_name": full_name.strip(),
        "phone": phone,
        "date_of_birth": date_of_birth,
        "gender": gender,
        "blood_type": blood_type,
        "allergies": allergies or [],
        "medical_conditions": medical_conditions or [],
        "emergency_contact": emergency_contact,
        "profile_image_url": None,
        "fcm_token": fcm_token,
        "notification_settings": {
            "medication_reminders": True,
            "refill_alerts": True,
            "weekly_reports": False,
        },
        "created_at": now,
        "updated_at": now,
    }
