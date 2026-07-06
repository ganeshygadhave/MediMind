"""
MedRem Backend — Chat Schemas
Request/Response models for AI assistant endpoints.
"""

from pydantic import BaseModel, Field
from typing import Optional


class ChatRequest(BaseModel):
    """Send a message to the AI assistant."""
    message: str = Field(..., min_length=1, max_length=2000)


class ChatResponse(BaseModel):
    """AI assistant response."""
    role: str = "assistant"
    content: str
    timestamp: str


class ChatHistoryResponse(BaseModel):
    """Chat history list response."""
    messages: list[dict]
    total: int
