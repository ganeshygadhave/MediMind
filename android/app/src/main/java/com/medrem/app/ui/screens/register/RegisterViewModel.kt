package com.medrem.app.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.dto.RegisterRequestDto
import com.medrem.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.messaging.FirebaseMessaging
import com.medrem.app.data.local.TokenManager
import kotlinx.coroutines.tasks.await

data class RegisterUiState(
    // Step 1: Account Info
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    // Step 2: Health Profile
    val dateOfBirth: String = "",
    val gender: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val medicalConditions: String = "",
    // UI state
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFieldChange(field: String, value: String) {
        uiState = when (field) {
            "fullName" -> uiState.copy(fullName = value, error = null)
            "email" -> uiState.copy(email = value, error = null)
            "password" -> uiState.copy(password = value, error = null)
            "confirmPassword" -> uiState.copy(confirmPassword = value, error = null)
            "phone" -> uiState.copy(phone = value, error = null)
            "dateOfBirth" -> uiState.copy(dateOfBirth = value, error = null)
            "gender" -> uiState.copy(gender = value, error = null)
            "bloodType" -> uiState.copy(bloodType = value, error = null)
            "allergies" -> uiState.copy(allergies = value, error = null)
            "medicalConditions" -> uiState.copy(medicalConditions = value, error = null)
            else -> uiState
        }
    }

    fun nextStep() {
        if (uiState.currentStep == 1) {
            // Validate Step 1
            if (uiState.fullName.isBlank() || uiState.email.isBlank() || uiState.password.isBlank() || uiState.phone.isBlank()) {
                uiState = uiState.copy(error = "Please fill in all required fields including mobile number.")
                return
            }
            if (uiState.phone.length < 10) {
                uiState = uiState.copy(error = "Please enter a valid 10-digit mobile number.")
                return
            }
            if (uiState.password != uiState.confirmPassword) {
                uiState = uiState.copy(error = "Passwords do not match.")
                return
            }
            if (uiState.password.length < 8) {
                uiState = uiState.copy(error = "Password must be at least 8 characters.")
                return
            }
            uiState = uiState.copy(currentStep = 2, error = null)
        }
    }

    fun previousStep() {
        if (uiState.currentStep > 1) {
            uiState = uiState.copy(currentStep = uiState.currentStep - 1, error = null)
        }
    }

    fun register() {
        if (uiState.dateOfBirth.isBlank() || uiState.gender.isBlank() || uiState.bloodType.isBlank()) {
            uiState = uiState.copy(error = "Please complete all required fields (Date of Birth, Gender, Blood Type).")
            return
        }
        if (uiState.dateOfBirth.length < 8) {
            uiState = uiState.copy(error = "Please enter a complete Date of Birth.")
            return
        }
        
        val formattedDate = "${uiState.dateOfBirth.substring(0, 4)}-${uiState.dateOfBirth.substring(4, 6)}-${uiState.dateOfBirth.substring(6, 8)}"

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // Capture FCM token
            var fcmToken: String? = null
            try {
                fcmToken = FirebaseMessaging.getInstance().token.await()
                fcmToken?.let { tokenManager.saveFCMToken(it) }
            } catch (e: Exception) {
                // If FCM fails, register still proceeds
            }

            val allergiesList = uiState.allergies
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val conditionsList = uiState.medicalConditions
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { mapOf("name" to it) }

            val request = RegisterRequestDto(
                email = uiState.email.trim(),
                password = uiState.password,
                fullName = uiState.fullName.trim(),
                phone = uiState.phone.trim(),
                dateOfBirth = formattedDate,
                gender = uiState.gender,
                bloodType = uiState.bloodType,
                allergies = allergiesList.ifEmpty { null },
                medicalConditions = conditionsList.ifEmpty { null },
                fcmToken = fcmToken
            )

            val result = authRepository.register(request)
            result.fold(
                onSuccess = {
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                },
                onFailure = {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = it.message ?: "Registration failed."
                    )
                }
            )
        }
    }
}
