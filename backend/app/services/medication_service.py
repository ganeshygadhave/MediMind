"""
MedRem Backend — Medication Service
Business logic for medication management.
"""

from fastapi import HTTPException, status

from app.models.medication import create_medication_document
from app.repositories import medication_repository
from app.utils.helpers import serialize_doc


async def create_medication(user_id: str, data: dict) -> dict:
    """Create a new medication entry for the user."""
    med_doc = create_medication_document(user_id=user_id, **data)
    med_id = await medication_repository.create_medication(med_doc)
    med_doc["_id"] = med_id
    return serialize_doc(med_doc)


async def get_all_medications(user_id: str) -> list[dict]:
    """Get all medications for the user."""
    medications = await medication_repository.find_medications_by_user(user_id)
    return [serialize_doc(med) for med in medications]


async def get_medication(medication_id: str, user_id: str) -> dict:
    """
    Get a specific medication by ID.

    Raises:
        HTTPException 404: If medication not found
    """
    medication = await medication_repository.find_medication_by_id(medication_id, user_id)
    if not medication:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medication not found."
        )
    return serialize_doc(medication)


async def update_medication(medication_id: str, user_id: str, update_data: dict) -> dict:
    """
    Update a medication entry.

    Raises:
        HTTPException 404: If medication not found
    """
    # Remove None values
    clean_data = {k: v for k, v in update_data.items() if v is not None}

    if not clean_data:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No fields to update."
        )

    updated = await medication_repository.update_medication(medication_id, user_id, clean_data)
    if not updated:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medication not found."
        )

    # Return updated medication
    medication = await medication_repository.find_medication_by_id(medication_id, user_id)
    return serialize_doc(medication)


async def delete_medication(medication_id: str, user_id: str) -> bool:
    """
    Delete a medication entry.

    Raises:
        HTTPException 404: If medication not found
    """
    deleted = await medication_repository.delete_medication(medication_id, user_id)
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medication not found."
        )
    return True
