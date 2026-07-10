package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * AI Assistant API endpoints.
 */
interface AiApi {
    @POST("api/ai/chat")
    suspend fun chat(@Body request: ChatRequestDto): Response<ChatMessageDto>

    @POST("api/ai/summarize-report")
    suspend fun summarizeReport(@Body request: ReportActionDto): Response<ReportSummaryResponseDto>

    @POST("api/ai/extract-medicines")
    suspend fun extractMedicines(@Body request: ReportActionDto): Response<ExtractMedicinesResponseDto>

    @POST("api/ai/summarize-medical-history")
    suspend fun summarizeMedicalHistory(@Body request: MedicalHistorySummarizeRequestDto): Response<MedicalHistorySummarizeResponseDto>
}
