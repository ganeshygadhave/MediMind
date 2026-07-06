"""
MedRem Backend — Medication Model
MongoDB document structure for the medications collection.
"""

from datetime import datetime
from typing import Optional


def create_medication_document(
    user_id: str,
    name: str,
    dosage: str,
    frequency: str,
    reminder_times: list[str],
    duration_type: str = "permanent",
    duration_start: Optional[str] = None,
    duration_end: Optional[str] = None,
    instructions: Optional[str] = None,
    is_prn: bool = False,
    is_active: bool = True,
) -> dict:
    """
    Create a medication document for MongoDB insertion.

    Args:
        user_id: The user's ObjectId as string
        name: Medication name (e.g., "Metformin")
        dosage: Dosage amount (e.g., "500mg")
        frequency: Frequency label (e.g., "Once Daily", "Twice Daily", "As Needed")
        reminder_times: List of reminder times (e.g., ["08:00", "20:00"])
        duration_type: "permanent" or "custom"
        duration_start: Start date (ISO string) if custom
        duration_end: End date (ISO string) if custom
        instructions: Additional instructions
        is_prn: Whether this is an as-needed (PRN) medication
        is_active: Whether the medication is currently active

    Returns:
        dict: Medication document ready for MongoDB insert
    """
    now = datetime.utcnow()
    return {
        "user_id": user_id,
        "name": name.strip(),
        "dosage": dosage.strip(),
        "frequency": frequency,
        "reminder_times": reminder_times,
        "duration_type": duration_type,
        "duration_start": duration_start,
        "duration_end": duration_end,
        "instructions": instructions,
        "is_prn": is_prn,
        "is_active": is_active,
        "created_at": now,
        "updated_at": now,
    }
