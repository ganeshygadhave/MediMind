package com.medrem.app.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.UserDto
import com.medrem.app.domain.repository.AuthRepository
import com.medrem.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDto? = null, 
    val isLoading: Boolean = true, 
    val error: String? = null,
    val isSavingHistory: Boolean = false,
    val historySaved: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {
    
    var uiState by mutableStateOf(ProfileUiState())
        private set

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            authRepository.getMe().fold(
                onSuccess = { uiState = uiState.copy(user = it, isLoading = false) },
                onFailure = { uiState = uiState.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun saveMedicalHistory(text: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isSavingHistory = true, error = null, historySaved = false)
            reportRepository.summarizeMedicalHistory(text).fold(
                onSuccess = {
                    uiState = uiState.copy(isSavingHistory = false, historySaved = true)
                    loadProfile() // Reload profile to reflect new summary in list
                },
                onFailure = {
                    uiState = uiState.copy(isSavingHistory = false, error = "Failed to save history: ${it.message}")
                }
            )
        }
    }
}
