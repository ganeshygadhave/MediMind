package com.medrem.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val medicationReminders: Boolean = true,
    val refillAlerts: Boolean = true,
    val weeklyReports: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    fun toggleMedicationReminders() { uiState = uiState.copy(medicationReminders = !uiState.medicationReminders) }
    fun toggleRefillAlerts() { uiState = uiState.copy(refillAlerts = !uiState.refillAlerts) }
    fun toggleWeeklyReports() { uiState = uiState.copy(weeklyReports = !uiState.weeklyReports) }

    fun logout() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoggingOut = true)
            authRepository.logout()
            uiState = uiState.copy(isLoggingOut = false, isLoggedOut = true)
        }
    }
}
