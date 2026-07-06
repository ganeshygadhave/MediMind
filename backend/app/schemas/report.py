"""
MedRem Backend — Report Schemas
Request/Response models for report endpoints.
"""

from pydantic import BaseModel
from typing import Optional


class ReportResponse(BaseModel):
    """Report response."""
    id: str
    user_id: str
    title: str
    report_type: str
    file_url: str
    ai_summary: Optional[str] = None
    extracted_data: Optional[dict] = None
    created_at: str
    updated_at: str


class ReportSummaryRequest(BaseModel):
    """Request AI summarization of a report."""
    report_id: str


class ExtractMedicinesRequest(BaseModel):
    """Request medicine extraction from prescription image."""
    report_id: str
