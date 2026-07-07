package com.medrem.app.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging
import com.medrem.app.data.local.TokenManager
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, error = null)
    }

    fun login() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all fields.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // Capture FCM token
            var fcmToken: String? = null
            try {
                fcmToken = FirebaseMessaging.getInstance().token.await()
                fcmToken?.let { tokenManager.saveFCMToken(it) }
            } catch (e: Exception) {
                // If FCM fails, login still proceeds
            }

            val result = authRepository.login(uiState.email.trim(), uiState.password, fcmToken)
            result.fold(
                onSuccess = {
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                },
                onFailure = {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = it.message ?: "Login failed. Please try again."
                    )
                }
            )
        }
    }
}
