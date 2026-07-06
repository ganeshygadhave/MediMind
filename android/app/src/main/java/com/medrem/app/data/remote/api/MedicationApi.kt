package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Medication API endpoints.
 */
interface MedicationApi {
    @POST("api/medications")
    suspend fun createMedication(@Body request: MedicationCreateDto): Response<MedicationDto>

    @GET("api/medications")
    suspend fun getAllMedications(): Response<List<MedicationDto>>

    @GET("api/medications/{id}")
    suspend fun getMedication(@Path("id") id: String): Response<MedicationDto>

    @PUT("api/medications/{id}")
    suspend fun updateMedication(
        @Path("id") id: String,
        @Body request: MedicationUpdateDto
    ): Response<MedicationDto>

    @DELETE("api/medications/{id}")
    suspend fun deleteMedication(@Path("id") id: String): Response<MessageResponseDto>
}
