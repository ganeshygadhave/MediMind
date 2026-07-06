package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Dashboard & Reminder API endpoints.
 */
interface DashboardApi {
    @GET("api/dashboard/stats")
    suspend fun getStats(): Response<DashboardStatsDto>

    @GET("api/dashboard/consistency")
    suspend fun getConsistency(): Response<TodayProgressDto>

    @GET("api/dashboard/recent-reports")
    suspend fun getRecentReports(): Response<List<RecentReportDto>>

    @POST("api/reminders/taken")
    suspend fun markTaken(@Body request: DoseLogRequestDto): Response<DoseLogResponseDto>

    @POST("api/reminders/skipped")
    suspend fun markSkipped(@Body request: DoseLogRequestDto): Response<DoseLogResponseDto>

    @POST("api/reminders/remind-later")
    suspend fun remindLater(@Body request: DoseLogRequestDto): Response<DoseLogResponseDto>
}
