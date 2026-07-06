"""
MedRem Backend — User Repository
Database access layer for the users collection.
"""

from bson import ObjectId
from typing import Optional
from datetime import datetime

from app.database import get_users_collection


async def find_user_by_email(email: str) -> Optional[dict]:
    """Find a user by email address."""
    collection = get_users_collection()
    return await collection.find_one({"email": email.lower().strip()})


async def find_user_by_id(user_id: str) -> Optional[dict]:
    """Find a user by their ObjectId."""
    collection = get_users_collection()
    return await collection.find_one({"_id": ObjectId(user_id)})


async def create_user(user_doc: dict) -> str:
    """Insert a new user document. Returns the inserted ID as string."""
    collection = get_users_collection()
    result = await collection.insert_one(user_doc)
    return str(result.inserted_id)


async def update_user(user_id: str, update_data: dict) -> bool:
    """Update user fields. Returns True if modified."""
    collection = get_users_collection()
    update_data["updated_at"] = datetime.utcnow()
    result = await collection.update_one(
        {"_id": ObjectId(user_id)},
        {"$set": update_data}
    )
    return result.modified_count > 0


async def delete_user(user_id: str) -> bool:
    """Delete a user by ID. Returns True if deleted."""
    collection = get_users_collection()
    result = await collection.delete_one({"_id": ObjectId(user_id)})
    return result.deleted_count > 0
