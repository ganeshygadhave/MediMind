package com.medrem.app.ui.screens.medications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.MedicationCreateDto
import com.medrem.app.domain.repository.MedicationRepository
import com.medrem.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedicationUiState(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "Once Daily",
    val reminderTime: String = "08:00",
    val durationType: String = "permanent",
    val instructions: String = "",
    val isPrn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    var uiState by mutableStateOf(AddMedicationUiState())
        private set

    fun onFieldChange(field: String, value: String) {
        uiState = when (field) {
            "name" -> uiState.copy(name = value, error = null)
            "dosage" -> uiState.copy(dosage = value, error = null)
            "frequency" -> uiState.copy(frequency = value, error = null)
            "reminderTime" -> uiState.copy(reminderTime = value, error = null)
            "durationType" -> uiState.copy(durationType = value, error = null)
            "instructions" -> uiState.copy(instructions = value, error = null)
            else -> uiState
        }
    }

    fun togglePrn() { uiState = uiState.copy(isPrn = !uiState.isPrn) }

    fun addMedication() {
        if (uiState.name.isBlank() || uiState.dosage.isBlank()) {
            uiState = uiState.copy(error = "Name and dosage are required.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val request = MedicationCreateDto(
                name = uiState.name.trim(),
                dosage = uiState.dosage.trim(),
                frequency = uiState.frequency,
                reminderTimes = listOf(uiState.reminderTime),
                durationType = uiState.durationType,
                instructions = uiState.instructions.ifBlank { null },
                isPrn = uiState.isPrn,
            )
            medicationRepository.create(request).fold(
                onSuccess = { uiState = uiState.copy(isLoading = false, isSuccess = true) },
                onFailure = { uiState = uiState.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun uploadAndExtract(filePath: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            reportRepository.upload(filePath, "Extracted Prescription", "prescription").fold(
                onSuccess = { report ->
                    reportRepository.extractMedicines(report.id).fold(
                        onSuccess = { extractRes ->
                            val meds = extractRes.medicines
                            if (meds.isNotEmpty()) {
                                val firstMed = meds[0]
                                uiState = uiState.copy(
                                    name = firstMed["name"] ?: "",
                                    dosage = firstMed["dosage"] ?: "",
                                    frequency = firstMed["frequency"] ?: uiState.frequency,
                                    instructions = firstMed["instructions"] ?: "",
                                    isLoading = false
                                )
                            } else {
                                uiState = uiState.copy(isLoading = false, error = "No medicines found.")
                            }
                        },
                        onFailure = { uiState = uiState.copy(isLoading = false, error = it.message) }
                    )
                },
                onFailure = { uiState = uiState.copy(isLoading = false, error = it.message) }
            )
        }
    }
}
