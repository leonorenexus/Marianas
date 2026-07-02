package com.leonoretech.marianas.data.repository

enum class Provider { OPENROUTER, GROQ, GOOGLE, CUSTOM1, CUSTOM2, CUSTOM3 }
enum class FormatStyle { OPENAI, GEMINI }

data class CustomSlotConfig(
    val name: String,
    val url: String,
    val key: String,
    val model: String,
    val formatStyle: FormatStyle
)

data class ProviderConfig(
    val activeProvider: Provider,
    val openrouterKey: String,
    val openrouterModel: String,
    val groqKey: String,
    val groqModel: String,
    val googleKey: String,
    val googleModel: String,
    val custom1: CustomSlotConfig,
    val custom2: CustomSlotConfig,
    val custom3: CustomSlotConfig,
    val systemPrompt: String,
    val streamEnabled: Boolean,
    val timeoutSeconds: Int
) {
    fun activeModel(): String = when (activeProvider) {
        Provider.OPENROUTER -> openrouterModel
        Provider.GROQ -> groqModel
        Provider.GOOGLE -> googleModel
        Provider.CUSTOM1 -> custom1.model
        Provider.CUSTOM2 -> custom2.model
        Provider.CUSTOM3 -> custom3.model
    }

    fun hasValidAuth(): Boolean = when (activeProvider) {
        Provider.OPENROUTER -> openrouterKey.isNotBlank()
        Provider.GROQ -> groqKey.isNotBlank()
        Provider.GOOGLE -> googleKey.isNotBlank()
        Provider.CUSTOM1 -> custom1.url.isNotBlank() // key optional for custom slots
        Provider.CUSTOM2 -> custom2.url.isNotBlank()
        Provider.CUSTOM3 -> custom3.url.isNotBlank()
    }

    fun activeCustomSlot(): CustomSlotConfig? = when (activeProvider) {
        Provider.CUSTOM1 -> custom1
        Provider.CUSTOM2 -> custom2
        Provider.CUSTOM3 -> custom3
        else -> null
    }
}

/** Result of a chat call: either successful text, or a human-readable error message. */
sealed class ModelResult {
    data class Success(val text: String) : ModelResult()
    data class Failure(val errorMessage: String) : ModelResult()
}
