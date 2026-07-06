package com.medrem.app.data.repository

import com.medrem.app.data.remote.api.DashboardApi
import com.medrem.app.data.remote.dto.*
import com.medrem.app.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi,
) : DashboardRepository {

    override suspend fun getStats(): Result<DashboardStatsDto> {
        return try {
            val response = api.getStats()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to fetch dashboard stats"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getConsistency(): Result<TodayProgressDto> {
        return try {
            val response = api.getConsistency()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to fetch consistency"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getRecentReports(): Result<List<RecentReportDto>> {
        return try {
            val response = api.getRecentReports()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to fetch recent reports"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markTaken(request: DoseLogRequestDto): Result<DoseLogResponseDto> {
        return try {
            val response = api.markTaken(request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to log dose"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markSkipped(request: DoseLogRequestDto): Result<DoseLogResponseDto> {
        return try {
            val response = api.markSkipped(request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to log dose"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun remindLater(request: DoseLogRequestDto): Result<DoseLogResponseDto> {
        return try {
            val response = api.remindLater(request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to snooze reminder"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
