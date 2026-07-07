package com.medrem.app.ui.screens.medications

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.MedicationCreateDto
import com.medrem.app.data.remote.dto.MedicationUpdateDto
import com.medrem.app.domain.repository.MedicationRepository
import com.medrem.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalId: String? = null, // Store server ID if editing
    var name: String = "",
    var dosage: String = "",
    var frequency: String = "Once Daily",
    var reminderTimes: List<String> = listOf("08:00"),
    var durationType: String = "custom",
    var durationDays: String = "7",
    var instructions: String = "",
    var isPrn: Boolean = false,
    var interactionWarning: String? = null,
    var isDuplicate: Boolean = false
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    // Using mutableStateListOf for high-performance real-time updates without clearing drafts
    val drafts = mutableStateListOf<MedicationDraft>(MedicationDraft())
    
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)
    var isEditMode by mutableStateOf(false)

    fun loadMedicationForEdit(id: String) {
        viewModelScope.launch {
            isLoading = true
            isEditMode = true
            medicationRepository.getById(id).fold(
                onSuccess = { med ->
                    drafts.clear()
                    drafts.add(MedicationDraft(
                        originalId = med.id,
                        name = med.name,
                        dosage = med.dosage,
                        frequency = med.frequency,
                        reminderTimes = med.reminderTimes,
                        instructions = med.instructions ?: "",
                        isPrn = med.isPrn,
                        durationType = med.durationType
                    ))
                    isLoading = false
                },
                onFailure = { 
                    error = "Failed to load medication data: ${it.message}"
                    isLoading = false
                }
            )
        }
    }

    fun updateDraft(id: String, update: (MedicationDraft) -> MedicationDraft) {
        val index = drafts.indexOfFirst { it.id == id }
        if (index != -1) {
            drafts[index] = update(drafts[index])
        }
    }

    fun addEmptyDraft() {
        drafts.add(MedicationDraft())
    }

    fun removeDraft(id: String) {
        if (drafts.size > 1) {
            drafts.removeAll { it.id == id }
        }
    }

    fun addMedication() {
        val invalidDraft = drafts.find { it.name.isBlank() || it.dosage.isBlank() }
        if (invalidDraft != null) {
            error = "All medicines must have a name and dosage."
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            
            var allSuccess = true
            drafts.forEach { draft ->
                val durationEndStr = if (draft.durationType == "custom") {
                    val days = draft.durationDays.toIntOrNull() ?: 7
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                } else null

                if (draft.originalId != null) {
                    // Update existing medication
                    val updateRequest = MedicationUpdateDto(
                        name = draft.name.trim(),
                        dosage = draft.dosage.trim(),
                        frequency = draft.frequency,
                        reminderTimes = draft.reminderTimes,
                        isPrn = draft.isPrn,
                    )
                    medicationRepository.update(draft.originalId, updateRequest).fold(
                        onSuccess = {},
                        onFailure = { allSuccess = false; error = it.message }
                    )
                } else {
                    // Create new medication
                    val request = MedicationCreateDto(
                        name = draft.name.trim(),
                        dosage = draft.dosage.trim(),
                        frequency = draft.frequency,
                        reminderTimes = draft.reminderTimes,
                        durationType = if (draft.durationType == "permanent") "permanent" else "custom",
                        durationEnd = durationEndStr,
                        instructions = draft.instructions.ifBlank { null },
                        isPrn = draft.isPrn,
                    )
                    medicationRepository.create(request).fold(
                        onSuccess = {},
                        onFailure = { allSuccess = false; error = it.message }
                    )
                }
            }
            
            if (allSuccess) {
                isLoading = false
                isSuccess = true
            } else {
                isLoading = false
                if (error == null) error = "Some medications failed to save."
            }
        }
    }

    fun uploadAndExtract(filePath: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            reportRepository.upload(filePath, "Extracted Prescription", "prescription").fold(
                onSuccess = { report ->
                    reportRepository.extractMedicines(report.id).fold(
                        onSuccess = { extractRes ->
                            val meds = extractRes.medicines
                            if (meds.isNotEmpty()) {
                                drafts.clear()
                                drafts.addAll(meds.map { med ->
                                    MedicationDraft(
                                        name = med.name,
                                        dosage = med.dosage,
                                        frequency = when (med.frequency.lowercase()) {
                                            "once daily", "twice daily", "thrice daily" -> med.frequency
                                            else -> "Once Daily"
                                        },
                                        instructions = med.instructions ?: "",
                                        interactionWarning = med.interactionWarning,
                                        isDuplicate = med.isDuplicate
                                    )
                                })
                                isLoading = false
                                if (meds.any { it.instructions == "IMAGE UNCLEAR" }) {
                                    error = "Some parts of the prescription are unclear. Please review the prefilled data."
                                }
                            } else {
                                isLoading = false
                                error = "No medicines found."
                            }
                        },
                        onFailure = { isLoading = false; error = it.message }
                    )
                },
                onFailure = { isLoading = false; error = it.message }
            )
        }
    }
}
