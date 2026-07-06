"""
MedRem Backend — Medication Routes
POST   /api/medications
GET    /api/medications
GET    /api/medications/{id}
PUT    /api/medications/{id}
DELETE /api/medications/{id}
"""

from fastapi import APIRouter, Depends

from app.schemas.medication import MedicationCreate, MedicationUpdate
from app.schemas.auth import MessageResponse
from app.services import medication_service
from app.middleware.auth_middleware import get_current_user

router = APIRouter(prefix="/api/medications", tags=["Medications"])


@router.post("")
async def create_medication(
    request: MedicationCreate,
    current_user: dict = Depends(get_current_user),
):
    """Add a new medication to the user's cabinet."""
    user_id = str(current_user["_id"])
    data = request.model_dump()
    return await medication_service.create_medication(user_id, data)


@router.get("")
async def get_all_medications(current_user: dict = Depends(get_current_user)):
    """Get all medications for the current user."""
    user_id = str(current_user["_id"])
    return await medication_service.get_all_medications(user_id)


@router.get("/{medication_id}")
async def get_medication(
    medication_id: str,
    current_user: dict = Depends(get_current_user),
):
    """Get a specific medication by ID."""
    user_id = str(current_user["_id"])
    return await medication_service.get_medication(medication_id, user_id)


@router.put("/{medication_id}")
async def update_medication(
    medication_id: str,
    request: MedicationUpdate,
    current_user: dict = Depends(get_current_user),
):
    """Update a medication entry."""
    user_id = str(current_user["_id"])
    update_data = request.model_dump(exclude_unset=True)
    return await medication_service.update_medication(medication_id, user_id, update_data)


@router.delete("/{medication_id}", response_model=MessageResponse)
async def delete_medication(
    medication_id: str,
    current_user: dict = Depends(get_current_user),
):
    """Delete a medication from the user's cabinet."""
    user_id = str(current_user["_id"])
    await medication_service.delete_medication(medication_id, user_id)
    return {"message": "Medication deleted successfully."}
