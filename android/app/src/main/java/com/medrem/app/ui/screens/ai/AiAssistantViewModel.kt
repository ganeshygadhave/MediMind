package com.medrem.app.ui.screens.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.remote.api.AiApi
import com.medrem.app.data.remote.dto.ChatMessageDto
import com.medrem.app.data.remote.dto.ChatRequestDto
import com.medrem.app.domain.repository.ReportRepository
import com.medrem.app.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.net.Uri

data class SelectedFileInfo(
    val uri: Uri,
    val name: String,
    val path: String
)

data class AiUiState(
    val messages: List<ChatMessageDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val selectedFile: SelectedFileInfo? = null,
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiApi: AiApi,
    private val reportRepository: ReportRepository,
) : ViewModel() {
    var uiState by mutableStateOf(AiUiState())
        private set

    fun onInputChange(text: String) { uiState = uiState.copy(inputText = text) }

    fun onFileSelected(context: Context, uri: Uri) {
        val path = FileUtils.getFilePathFromUri(context, uri)
        if (path != null) {
            uiState = uiState.copy(selectedFile = SelectedFileInfo(uri, path.substringAfterLast("/"), path))
        }
    }

    fun clearSelectedFile() { uiState = uiState.copy(selectedFile = null) }

    fun sendMessage() {
        val text = uiState.inputText.trim()
        val file = uiState.selectedFile
        if (text.isBlank() && file == null) return

        val messageContent = if (file != null && text.isBlank()) "Attached: ${file.name}" else text
        val userMsg = ChatMessageDto(role = "user", content = messageContent, timestamp = "")
        uiState = uiState.copy(messages = uiState.messages + userMsg, inputText = "", selectedFile = null, isLoading = true)

        viewModelScope.launch {
            try {
                var finalPrompt = text
                if (file != null) {
                    val titleResult = reportRepository.getNextTitle("chatbot")
                    val title = titleResult.getOrDefault("Chatbot File")
                    val uploadRes = reportRepository.upload(file.path, title, "medical_report")
                    uploadRes.fold(
                        onSuccess = { report ->
                            finalPrompt = "[Report Attached: ${report.title}]\n$text"
                        },
                        onFailure = { 
                            // If upload fails, we still try to send the message but maybe notify
                        }
                    )
                }

                val response = aiApi.chat(ChatRequestDto(finalPrompt))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val assistantMsg = body.copy(role = "assistant")
                    uiState = uiState.copy(messages = uiState.messages + assistantMsg, isLoading = false)
                } else {
                    val errorMsg = ChatMessageDto("assistant", "Sorry, I couldn't process your request. Please try again.")
                    uiState = uiState.copy(messages = uiState.messages + errorMsg, isLoading = false)
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessageDto("assistant", "Connection error. Please check your internet and try again.")
                uiState = uiState.copy(messages = uiState.messages + errorMsg, isLoading = false)
            }
        }
    }

    fun sendQuickAction(action: String) {
        uiState = uiState.copy(inputText = action)
        sendMessage()
    }
}
