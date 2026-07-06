"""
MedRem Backend — Chat Repository
Database access layer for the chat_history collection.
"""

from bson import ObjectId

from app.database import get_chat_history_collection


async def save_message(message_doc: dict) -> str:
    """Save a chat message. Returns the inserted ID as string."""
    collection = get_chat_history_collection()
    result = await collection.insert_one(message_doc)
    return str(result.inserted_id)


async def get_chat_history(user_id: str, limit: int = 50) -> list[dict]:
    """Get chat history for a user, ordered by timestamp ascending."""
    collection = get_chat_history_collection()
    cursor = collection.find(
        {"user_id": user_id}
    ).sort("timestamp", 1).limit(limit)
    return await cursor.to_list(length=limit)


async def get_recent_messages(user_id: str, limit: int = 10) -> list[dict]:
    """Get the most recent messages for context injection into Gemini."""
    collection = get_chat_history_collection()
    cursor = collection.find(
        {"user_id": user_id}
    ).sort("timestamp", -1).limit(limit)
    messages = await cursor.to_list(length=limit)
    # Reverse to get chronological order
    messages.reverse()
    return messages


async def delete_all_user_chats(user_id: str) -> int:
    """Delete all chat history for a user. Returns count deleted."""
    collection = get_chat_history_collection()
    result = await collection.delete_many({"user_id": user_id})
    return result.deleted_count
