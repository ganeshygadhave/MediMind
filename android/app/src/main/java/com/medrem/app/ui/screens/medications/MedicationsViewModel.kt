package com.medrem.app.ui.screens.medications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.MedicationDto
import com.medrem.app.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationsUiState(
    val medications: List<MedicationDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    var uiState by mutableStateOf(MedicationsUiState())
        private set

    init { loadMedications() }

    fun loadMedications() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            medicationRepository.getAll().fold(
                onSuccess = { uiState = uiState.copy(medications = it, isLoading = false) },
                onFailure = { uiState = uiState.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun onSearchChange(query: String) { uiState = uiState.copy(searchQuery = query) }

    fun deleteMedication(id: String) {
        viewModelScope.launch {
            medicationRepository.delete(id)
            loadMedications()
        }
    }

    val filteredMedications: List<MedicationDto>
        get() {
            val q = uiState.searchQuery.lowercase()
            return if (q.isBlank()) uiState.medications
            else uiState.medications.filter { it.name.lowercase().contains(q) }
        }
}
