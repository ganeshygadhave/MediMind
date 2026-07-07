"""
MedRem Backend — Report Repository
Database access layer for the reports collection.
"""

from bson import ObjectId
from typing import Optional
from datetime import datetime

from app.database import get_reports_collection


async def create_report(report_doc: dict) -> str:
    """Insert a new report document. Returns the inserted ID as string."""
    collection = get_reports_collection()
    result = await collection.insert_one(report_doc)
    return str(result.inserted_id)


async def find_reports_by_user(user_id: str, limit: int = 50) -> list[dict]:
    """Get all reports for a user, ordered by newest first."""
    collection = get_reports_collection()
    cursor = collection.find({"user_id": user_id}).sort("created_at", -1).limit(limit)
    return await cursor.to_list(length=limit)


async def find_report_by_id(report_id: str, user_id: str) -> Optional[dict]:
    """Find a specific report by ID, scoped to a user."""
    collection = get_reports_collection()
    return await collection.find_one({
        "_id": ObjectId(report_id),
        "user_id": user_id
    })


async def update_report(report_id: str, user_id: str, update_data: dict) -> bool:
    """Update report fields. Returns True if modified."""
    collection = get_reports_collection()
    update_data["updated_at"] = datetime.utcnow()
    result = await collection.update_one(
        {"_id": ObjectId(report_id), "user_id": user_id},
        {"$set": update_data}
    )
    return result.modified_count > 0


async def delete_report(report_id: str, user_id: str) -> bool:
    """Delete a report by ID, scoped to a user. Returns True if deleted."""
    collection = get_reports_collection()
    result = await collection.delete_one({
        "_id": ObjectId(report_id),
        "user_id": user_id
    })
    return result.deleted_count > 0


async def find_recent_reports(user_id: str, limit: int = 3) -> list[dict]:
    """Get the most recent reports for dashboard display."""
    collection = get_reports_collection()
    cursor = collection.find(
        {"user_id": user_id},
        {"_id": 1, "title": 1, "report_type": 1, "ai_summary": 1, "extracted_data": 1, "created_at": 1}
    ).sort("created_at", -1).limit(limit)
    return await cursor.to_list(length=limit)


async def delete_all_user_reports(user_id: str) -> int:
    """Delete all reports for a user. Returns count deleted."""
    collection = get_reports_collection()
    result = await collection.delete_many({"user_id": user_id})
    return result.deleted_count
