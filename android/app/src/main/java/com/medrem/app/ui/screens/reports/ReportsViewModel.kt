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

data class ReportsUiState(
    val reports: List<ReportDto> = emptyList(), 
    val isLoading: Boolean = true, 
    val isUploading: Boolean = false,
    val error: String? = null,
    val pendingImages: List<android.net.Uri> = emptyList()
)

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

    fun addImagesToQueue(uris: List<android.net.Uri>) {
        val current = uiState.pendingImages.toMutableList()
        uris.forEach { if (current.size < 5) current.add(it) }
        uiState = uiState.copy(pendingImages = current)
    }

    fun removeImageFromQueue(uri: android.net.Uri) {
        val current = uiState.pendingImages.filter { it != uri }
        uiState = uiState.copy(pendingImages = current)
    }

    fun uploadQueue(context: android.content.Context) {
        if (uiState.pendingImages.isEmpty()) return
        
        viewModelScope.launch {
            uiState = uiState.copy(isUploading = true, error = null)
            
            // For now, we take the first image
            val uri = uiState.pendingImages.first()
            val path = com.medrem.app.util.FileUtils.getFilePathFromUri(context, uri)
            
            if (path != null) {
                val titleResult = reportRepository.getNextTitle("report")
                val title = titleResult.getOrDefault("Report")

                reportRepository.upload(path, title, "medical_record").fold(
                    onSuccess = { report ->
                        // Automatically generate and save the summary
                        reportRepository.summarize(report.id).fold(
                            onSuccess = {
                                uiState = uiState.copy(pendingImages = emptyList(), isUploading = false)
                                loadReports()
                            },
                            onFailure = {
                                // Even if summarization fails, complete the upload
                                uiState = uiState.copy(pendingImages = emptyList(), isUploading = false)
                                loadReports()
                            }
                        )
                    },
                    onFailure = { uiState = uiState.copy(error = it.message, isUploading = false) }
                )
            } else {
                uiState = uiState.copy(error = "Could not process image", isUploading = false)
            }
        }
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

    fun renameReport(id: String, newTitle: String) {
        viewModelScope.launch {
            reportRepository.rename(id, newTitle).fold(
                onSuccess = { loadReports() },
                onFailure = { uiState = uiState.copy(error = it.message) }
            )
        }
    }
}
