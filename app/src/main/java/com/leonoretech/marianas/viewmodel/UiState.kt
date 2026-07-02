package com.leonoretech.marianas.viewmodel

import com.leonoretech.marianas.data.repository.FormatStyle
import com.leonoretech.marianas.data.repository.Provider

/**
 * A single chat line as displayed in the UI. Distinct from ChatMessage
 * (the network domain model) because the UI also needs to know error state,
 * a stable id for Compose keys/actions (copy/save), and a "still streaming"
 * flag for the live-updating assistant reply.
 */
data class ChatUiMessage(
    val id: Long,
    val role: MessageRole,
    val content: String,
    val isError: Boolean = false,
    val imagePaths: List<String> = emptyList(),
    val isStreaming: Boolean = false
)

enum class MessageRole { USER, ASSISTANT }

data class SessionUiItem(
    val id: String,
    val title: String,
    val isActive: Boolean
)

/** Snapshot of everything the chat screen needs to render at once. */
data class ChatScreenState(
    val messages: List<ChatUiMessage> = emptyList(),
    val isSending: Boolean = false,
    val statusText: String = "siap",
    val activeProviderLabel: String = "no model set",
    val pendingImagePaths: List<String> = emptyList()
)

data class CustomSlotUiState(
    val name: String = "Custom",
    val url: String = "",
    val key: String = "",
    val model: String = "",
    val formatStyle: FormatStyle = FormatStyle.OPENAI
)

/** Snapshot for the "Provider" dashboard — AI provider selection and credentials only. */
data class ConfigScreenState(
    val activeProvider: Provider = Provider.OPENROUTER,
    val openrouterKey: String = "",
    val openrouterModel: String = "",
    val groqKey: String = "",
    val groqModel: String = "",
    val googleKey: String = "",
    val googleModel: String = "gemini-3.5-flash",
    val custom1: CustomSlotUiState = CustomSlotUiState(name = "Custom 1"),
    val custom2: CustomSlotUiState = CustomSlotUiState(name = "Custom 2"),
    val custom3: CustomSlotUiState = CustomSlotUiState(name = "Custom 3"),
    val systemPrompt: String = "",
    val streamEnabled: Boolean = true,
    val timeoutSeconds: Int = 300,
    val fallbackEnabled: Boolean = true,
    val connectionTestResult: ConnectionTestResult = ConnectionTestResult.Idle
)

/** Snapshot for the "Data & Sesi" dashboard — session history management (local only, no cloud sync). */
data class DataScreenState(
    val sessions: List<SessionUiItem> = emptyList()
)

sealed class ConnectionTestResult {
    object Idle : ConnectionTestResult()
    object Testing : ConnectionTestResult()
    data class Success(val message: String) : ConnectionTestResult()
    data class Failure(val message: String) : ConnectionTestResult()
}
