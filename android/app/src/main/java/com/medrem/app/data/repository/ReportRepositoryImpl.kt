package com.medrem.app.data.repository

import com.medrem.app.data.remote.api.ReportApi
import com.medrem.app.data.remote.api.AiApi
import com.medrem.app.data.remote.dto.*
import com.medrem.app.domain.repository.ReportRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
    private val aiApi: AiApi,
) : ReportRepository {

    override suspend fun getAll(): Result<List<ReportDto>> {
        return try {
            val response = reportApi.getAllReports()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to fetch reports"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getById(id: String): Result<ReportDto> {
        return try {
            val response = reportApi.getReport(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Report not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun upload(filePath: String, title: String, reportType: String): Result<ReportDto> {
        return try {
            val file = File(filePath)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = reportType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = reportApi.uploadReport(filePart, titlePart, typePart)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to upload report"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun delete(id: String): Result<MessageResponseDto> {
        return try {
            val response = reportApi.deleteReport(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to delete report"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun summarize(reportId: String): Result<ReportSummaryResponseDto> {
        return try {
            val response = aiApi.summarizeReport(ReportActionDto(reportId))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to summarize report"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun extractMedicines(reportId: String): Result<ExtractMedicinesResponseDto> {
        return try {
            val response = aiApi.extractMedicines(ReportActionDto(reportId))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to extract medicines"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun rename(id: String, newTitle: String): Result<ReportDto> {
        return try {
            val response = reportApi.renameReport(id, RenameReportRequestDto(newTitle))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to rename report"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getNextTitle(source: String): Result<String> {
        return try {
            val response = reportApi.getNextTitle(AutoTitleRequestDto(source))
            if (response.isSuccessful) Result.success(response.body()!!.title)
            else Result.failure(Exception("Failed to get next title"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun summarizeMedicalHistory(text: String): Result<MedicalHistorySummarizeResponseDto> {
        return try {
            val response = aiApi.summarizeMedicalHistory(MedicalHistorySummarizeRequestDto(text))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to summarize medical history"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
