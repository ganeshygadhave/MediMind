"""
MedRem Backend — Utility Helpers
Common utility functions used across the application.
"""

from bson import ObjectId
from datetime import datetime, timezone
from typing import Any


def to_object_id(id_str: str) -> ObjectId:
    """Convert a string to a BSON ObjectId."""
    return ObjectId(id_str)


def serialize_doc(doc: dict) -> dict:
    """
    Serialize a MongoDB document for JSON response.
    Converts ObjectId fields to strings.
    """
    if doc is None:
        return None
    serialized = {}
    for key, value in doc.items():
        if isinstance(value, ObjectId):
            serialized[key] = str(value)
        elif isinstance(value, datetime):
            serialized[key] = value.isoformat()
        elif isinstance(value, dict):
            serialized[key] = serialize_doc(value)
        elif isinstance(value, list):
            serialized[key] = [
                serialize_doc(item) if isinstance(item, dict)
                else str(item) if isinstance(item, ObjectId)
                else item
                for item in value
            ]
        else:
            serialized[key] = value
    # Rename _id to id
    if "_id" in serialized:
        serialized["id"] = serialized.pop("_id")
    return serialized


def utc_now() -> datetime:
    """Get current UTC datetime."""
    return datetime.now(timezone.utc)
