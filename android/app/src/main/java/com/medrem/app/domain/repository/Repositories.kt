package com.medrem.app.domain.repository

import com.medrem.app.data.remote.dto.*

/**
 * Auth repository interface.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String, fcmToken: String? = null): Result<AuthResponseDto>
    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto>
    suspend fun getMe(): Result<UserDto>
    suspend fun logout()
}

/**
 * Medication repository interface.
 */
interface MedicationRepository {
    suspend fun getAll(): Result<List<MedicationDto>>
    suspend fun getById(id: String): Result<MedicationDto>
    suspend fun create(request: MedicationCreateDto): Result<MedicationDto>
    suspend fun update(id: String, request: MedicationUpdateDto): Result<MedicationDto>
    suspend fun delete(id: String): Result<MessageResponseDto>
}

/**
 * Report repository interface.
 */
interface ReportRepository {
    suspend fun getAll(): Result<List<ReportDto>>
    suspend fun getById(id: String): Result<ReportDto>
    suspend fun upload(filePath: String, title: String, reportType: String): Result<ReportDto>
    suspend fun delete(id: String): Result<MessageResponseDto>
    suspend fun summarize(reportId: String): Result<ReportSummaryResponseDto>
    suspend fun extractMedicines(reportId: String): Result<ExtractMedicinesResponseDto>
}

/**
 * Dashboard repository interface.
 */
interface DashboardRepository {
    suspend fun getStats(): Result<DashboardStatsDto>
    suspend fun getConsistency(): Result<TodayProgressDto>
    suspend fun getRecentReports(): Result<List<RecentReportDto>>
    suspend fun markTaken(request: DoseLogRequestDto): Result<DoseLogResponseDto>
    suspend fun markSkipped(request: DoseLogRequestDto): Result<DoseLogResponseDto>
    suspend fun remindLater(request: DoseLogRequestDto): Result<DoseLogResponseDto>
}
