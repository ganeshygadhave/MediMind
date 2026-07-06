package com.medrem.app.ui.screens.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.ReportDto
import com.medrem.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportDetailUiState(
    val report: ReportDto? = null,
    val isLoading: Boolean = true,
    val isSummarizing: Boolean = false,
    val isExtracting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var uiState by mutableStateOf(ReportDetailUiState())
        private set

    private val reportId: String = savedStateHandle["reportId"] ?: ""

    init { loadReport() }

    private fun loadReport() {
        viewModelScope.launch {
            reportRepository.getById(reportId).fold(
                onSuccess = { uiState = uiState.copy(report = it, isLoading = false) },
                onFailure = { uiState = uiState.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun summarize() {
        viewModelScope.launch {
            uiState = uiState.copy(isSummarizing = true)
            reportRepository.summarize(reportId).fold(
                onSuccess = { uiState = uiState.copy(report = uiState.report?.copy(aiSummary = it.summary), isSummarizing = false) },
                onFailure = { uiState = uiState.copy(isSummarizing = false, error = it.message) }
            )
        }
    }

    fun extractMedicines() {
        viewModelScope.launch {
            uiState = uiState.copy(isExtracting = true)
            reportRepository.extractMedicines(reportId).fold(
                onSuccess = { uiState = uiState.copy(isExtracting = false) },
                onFailure = { uiState = uiState.copy(isExtracting = false, error = it.message) }
            )
        }
    }
}
