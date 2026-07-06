package com.medrem.app.ui.screens.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.api.AiApi
import com.medrem.app.data.remote.dto.ChatMessageDto
import com.medrem.app.data.remote.dto.ChatRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiUiState(
    val messages: List<ChatMessageDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiApi: AiApi,
) : ViewModel() {
    var uiState by mutableStateOf(AiUiState())
        private set

    fun onInputChange(text: String) { uiState = uiState.copy(inputText = text) }

    fun sendMessage() {
        val text = uiState.inputText.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessageDto(role = "user", content = text, timestamp = "")
        uiState = uiState.copy(messages = uiState.messages + userMsg, inputText = "", isLoading = true)

        viewModelScope.launch {
            try {
                val response = aiApi.chat(ChatRequestDto(text))
                if (response.isSuccessful) {
                    val assistantMsg = response.body()!!
                    uiState = uiState.copy(messages = uiState.messages + assistantMsg, isLoading = false)
                } else {
                    val errorMsg = ChatMessageDto("assistant", "Sorry, I couldn't process your request. Please try again.", "")
                    uiState = uiState.copy(messages = uiState.messages + errorMsg, isLoading = false)
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessageDto("assistant", "Connection error. Please check your internet and try again.", "")
                uiState = uiState.copy(messages = uiState.messages + errorMsg, isLoading = false)
            }
        }
    }

    fun sendQuickAction(action: String) {
        uiState = uiState.copy(inputText = action)
        sendMessage()
    }
}
