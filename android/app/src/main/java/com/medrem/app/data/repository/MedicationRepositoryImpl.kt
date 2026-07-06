package com.medrem.app.data.repository

import com.medrem.app.data.remote.api.MedicationApi
import com.medrem.app.data.remote.dto.*
import com.medrem.app.domain.repository.MedicationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val api: MedicationApi,
) : MedicationRepository {

    override suspend fun getAll(): Result<List<MedicationDto>> {
        return try {
            val response = api.getAllMedications()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to fetch medications"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getById(id: String): Result<MedicationDto> {
        return try {
            val response = api.getMedication(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Medication not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun create(request: MedicationCreateDto): Result<MedicationDto> {
        return try {
            val response = api.createMedication(request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to create medication"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun update(id: String, request: MedicationUpdateDto): Result<MedicationDto> {
        return try {
            val response = api.updateMedication(id, request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to update medication"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun delete(id: String): Result<MessageResponseDto> {
        return try {
            val response = api.deleteMedication(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Failed to delete medication"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
