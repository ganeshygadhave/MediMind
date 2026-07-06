"""
MedRem Backend — Chat Model
MongoDB document structure for the chat_history collection.
"""

from datetime import datetime


def create_chat_message_document(
    user_id: str,
    role: str,
    content: str,
    metadata: dict = None,
) -> dict:
    """
    Create a chat message document for MongoDB insertion.

    Args:
        user_id: The user's ObjectId as string
        role: "user" or "assistant"
        content: The message content
        metadata: Optional metadata (e.g., context used for generation)

    Returns:
        dict: Chat message document ready for MongoDB insert
    """
    return {
        "user_id": user_id,
        "role": role,
        "content": content,
        "metadata": metadata or {},
        "timestamp": datetime.utcnow(),
    }
