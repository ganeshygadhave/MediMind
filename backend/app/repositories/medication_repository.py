"""
MedRem Backend — Medication Repository
Database access layer for the medications collection.
"""

from bson import ObjectId
from typing import Optional
from datetime import datetime

from app.database import get_medications_collection


async def create_medication(med_doc: dict) -> str:
    """Insert a new medication document. Returns the inserted ID as string."""
    collection = get_medications_collection()
    result = await collection.insert_one(med_doc)
    return str(result.inserted_id)


async def find_medications_by_user(user_id: str, active_only: bool = False) -> list[dict]:
    """Get all medications for a user."""
    collection = get_medications_collection()
    query = {"user_id": user_id}
    if active_only:
        query["is_active"] = True
    cursor = collection.find(query).sort("created_at", -1)
    return await cursor.to_list(length=100)


async def find_medication_by_id(medication_id: str, user_id: str) -> Optional[dict]:
    """Find a specific medication by ID, scoped to a user."""
    collection = get_medications_collection()
    return await collection.find_one({
        "_id": ObjectId(medication_id),
        "user_id": user_id
    })


async def update_medication(medication_id: str, user_id: str, update_data: dict) -> bool:
    """Update medication fields. Returns True if modified."""
    collection = get_medications_collection()
    update_data["updated_at"] = datetime.utcnow()
    result = await collection.update_one(
        {"_id": ObjectId(medication_id), "user_id": user_id},
        {"$set": update_data}
    )
    return result.modified_count > 0


async def delete_medication(medication_id: str, user_id: str) -> bool:
    """Delete a medication by ID, scoped to a user. Returns True if deleted."""
    collection = get_medications_collection()
    result = await collection.delete_one({
        "_id": ObjectId(medication_id),
        "user_id": user_id
    })
    return result.deleted_count > 0


async def count_active_medications(user_id: str) -> int:
    """Count active medications for a user."""
    collection = get_medications_collection()
    return await collection.count_documents({
        "user_id": user_id,
        "is_active": True
    })


async def delete_all_user_medications(user_id: str) -> int:
    """Delete all medications for a user. Returns count deleted."""
    collection = get_medications_collection()
    result = await collection.delete_many({"user_id": user_id})
    return result.deleted_count
