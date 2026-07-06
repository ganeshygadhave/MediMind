package com.medrem.app.ui.screens.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.ReportDto
import com.medrem.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(val reports: List<ReportDto> = emptyList(), val isLoading: Boolean = true, val error: String? = null)

@HiltViewModel
class ReportsViewModel @Inject constructor(private val reportRepository: ReportRepository) : ViewModel() {
    var uiState by mutableStateOf(ReportsUiState())
        private set

    init { loadReports() }

    fun loadReports() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            reportRepository.getAll().fold(
                onSuccess = { uiState = uiState.copy(reports = it, isLoading = false) },
                onFailure = { uiState = uiState.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun deleteReport(id: String) {
        viewModelScope.launch { reportRepository.delete(id); loadReports() }
    }

    fun uploadReport(filePath: String, title: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            reportRepository.upload(filePath, title, "other").fold(
                onSuccess = { loadReports() },
                onFailure = { uiState = uiState.copy(error = it.message, isLoading = false) }
            )
        }
    }
}
