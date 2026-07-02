package com.leonoretech.marianas.viewmodel

import com.leonoretech.marianas.data.db.ConfigEntity
import com.leonoretech.marianas.data.db.MessageEntity
import com.leonoretech.marianas.data.db.SessionEntity
import com.leonoretech.marianas.data.repository.AttachedImage
import com.leonoretech.marianas.data.repository.ChatMessage
import com.leonoretech.marianas.data.repository.CustomSlotConfig
import com.leonoretech.marianas.data.repository.FormatStyle
import com.leonoretech.marianas.data.repository.ImagePathsJson
import com.leonoretech.marianas.data.repository.Provider
import com.leonoretech.marianas.data.repository.ProviderConfig
import com.leonoretech.marianas.data.repository.Role

private fun FormatStyle.toDbString(): String = if (this == FormatStyle.GEMINI) "gemini" else "openai"
private fun String.toFormatStyle(): FormatStyle = if (this == "gemini") FormatStyle.GEMINI else FormatStyle.OPENAI

fun ConfigEntity.toProviderConfig(): ProviderConfig = ProviderConfig(
    activeProvider = Provider.valueOf(activeProvider.uppercase()),
    openrouterKey = openrouterKey,
    openrouterModel = openrouterModel,
    groqKey = groqKey,
    groqModel = groqModel,
    googleKey = googleKey,
    googleModel = googleModel,
    custom1 = CustomSlotConfig(custom1Name, custom1Url, custom1Key, custom1Model, custom1FormatStyle.toFormatStyle()),
    custom2 = CustomSlotConfig(custom2Name, custom2Url, custom2Key, custom2Model, custom2FormatStyle.toFormatStyle()),
    custom3 = CustomSlotConfig(custom3Name, custom3Url, custom3Key, custom3Model, custom3FormatStyle.toFormatStyle()),
    systemPrompt = systemPrompt,
    streamEnabled = streamEnabled,
    timeoutSeconds = timeoutSeconds
)

fun ConfigEntity.toConfigScreenState(fallbackEnabled: Boolean): ConfigScreenState = ConfigScreenState(
    activeProvider = Provider.valueOf(activeProvider.uppercase()),
    openrouterKey = openrouterKey,
    openrouterModel = openrouterModel,
    groqKey = groqKey,
    groqModel = groqModel,
    googleKey = googleKey,
    googleModel = googleModel,
    custom1 = CustomSlotUiState(custom1Name, custom1Url, custom1Key, custom1Model, custom1FormatStyle.toFormatStyle()),
    custom2 = CustomSlotUiState(custom2Name, custom2Url, custom2Key, custom2Model, custom2FormatStyle.toFormatStyle()),
    custom3 = CustomSlotUiState(custom3Name, custom3Url, custom3Key, custom3Model, custom3FormatStyle.toFormatStyle()),
    systemPrompt = systemPrompt,
    streamEnabled = streamEnabled,
    timeoutSeconds = timeoutSeconds,
    fallbackEnabled = fallbackEnabled
)

fun MessageEntity.toChatUiMessage(): ChatUiMessage = ChatUiMessage(
    id = id,
    role = if (role == "user") MessageRole.USER else MessageRole.ASSISTANT,
    content = content,
    isError = isError,
    imagePaths = ImagePathsJson.decode(imagePathsJson)
)

/**
 * Converts a stored MessageEntity into the generic ChatMessage used for
 * network calls. Images are re-read from disk and base64-encoded here,
 * via the supplied encoder function, since MessageEntity only stores file
 * paths (not the encoded bytes) to keep the database small.
 */
fun MessageEntity.toChatMessage(imageEncoder: (String) -> AttachedImage?): ChatMessage {
    val role = when (this.role) {
        "system" -> Role.SYSTEM
        "user" -> Role.USER
        else -> Role.ASSISTANT
    }
    val images = ImagePathsJson.decode(imagePathsJson).mapNotNull(imageEncoder)
    return ChatMessage(role = role, content = content, images = images)
}

fun SessionEntity.toSessionUiItem(activeId: String?): SessionUiItem = SessionUiItem(
    id = id,
    title = title,
    isActive = id == activeId
)
