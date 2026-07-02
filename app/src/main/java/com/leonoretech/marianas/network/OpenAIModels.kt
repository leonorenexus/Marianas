package com.leonoretech.marianas.network

import com.google.gson.annotations.SerializedName

/**
 * Request/response shapes for any OpenAI Chat Completions-compatible provider
 * (OpenRouter, Groq, and user-defined Custom endpoints all share this format).
 */

data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val stream: Boolean = false
)

/**
 * `content` can be either a plain string (text-only message) or a list of
 * content parts (when images are attached). Gson serializes whichever type
 * is assigned at runtime, since we declare it as `Any`.
 */
data class OpenAIMessage(
    val role: String,
    val content: Any // String OR List<OpenAIContentPart>
)

data class OpenAIContentPart(
    val type: String, // "text" | "image_url"
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: OpenAIImageUrl? = null
)

data class OpenAIImageUrl(
    val url: String // data URI: "data:image/jpeg;base64,...."
)

data class OpenAIChatResponse(
    val id: String? = null,
    val choices: List<OpenAIChoice>? = null,
    val error: OpenAIError? = null
)

data class OpenAIChoice(
    val index: Int? = null,
    val message: OpenAIResponseMessage? = null,
    val delta: OpenAIResponseMessage? = null, // used in streaming chunks
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class OpenAIResponseMessage(
    val role: String? = null,
    val content: String? = null
)

data class OpenAIError(
    val message: String? = null,
    val code: Int? = null
)
