package com.leonoretech.marianas.network

import com.google.gson.annotations.SerializedName

/**
 * Request/response shapes for Google's native Gemini API
 * (generativelanguage.googleapis.com — NOT OpenAI-compatible).
 *
 * Key differences from the OpenAI format:
 * - Roles are "user" / "model" (not "assistant")
 * - No "system" role inside contents — goes in a separate systemInstruction field
 * - Images use inline_data with base64 (no data URI prefix), not image_url
 */

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiSystemInstruction(
    val parts: List<GeminiPart>
)

data class GeminiGenerationConfig(
    val maxOutputTokens: Int? = null
)

data class GeminiContent(
    val role: String, // "user" | "model"
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    @SerializedName("inline_data") val inlineData: GeminiInlineData? = null,
    val thought: Boolean? = null // present on "thinking" parts we want to filter out
)

data class GeminiInlineData(
    @SerializedName("mime_type") val mimeType: String,
    val data: String // raw base64, no "data:image/...;base64," prefix
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

data class GeminiError(
    val message: String? = null,
    val code: Int? = null
)
