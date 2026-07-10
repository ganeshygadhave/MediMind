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


class RenameReportRequest(BaseModel):
    """Request to rename a report."""
    title: str


class AutoTitleRequest(BaseModel):
    """Request the next auto-generated title for a given source."""
    source: str  # 'prescription' | 'report' | 'chatbot'


class AutoTitleResponse(BaseModel):
    """Response with the next auto-generated title."""
    title: str


class MedicalHistorySummarizeRequest(BaseModel):
    """Request AI summarization of free-text medical history."""
    text: str
