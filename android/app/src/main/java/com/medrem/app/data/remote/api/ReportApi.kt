package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Report API endpoints.
 */
interface ReportApi {
    @Multipart
    @POST("api/reports/upload")
    suspend fun uploadReport(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("report_type") reportType: RequestBody,
    ): Response<ReportDto>

    @GET("api/reports")
    suspend fun getAllReports(): Response<List<ReportDto>>

    @GET("api/reports/{id}")
    suspend fun getReport(@Path("id") id: String): Response<ReportDto>

    @DELETE("api/reports/{id}")
    suspend fun deleteReport(@Path("id") id: String): Response<MessageResponseDto>

    @PATCH("api/reports/{id}/rename")
    suspend fun renameReport(
        @Path("id") id: String,
        @Body request: RenameReportRequestDto
    ): Response<ReportDto>

    @POST("api/reports/next-title")
    suspend fun getNextTitle(
        @Body request: AutoTitleRequestDto
    ): Response<AutoTitleResponseDto>
}
