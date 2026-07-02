package com.leonoretech.marianas.data.repository

import com.leonoretech.marianas.network.ApiClientFactory
import com.leonoretech.marianas.network.GeminiContent
import com.leonoretech.marianas.network.GeminiInlineData
import com.leonoretech.marianas.network.GeminiPart
import com.leonoretech.marianas.network.GeminiRequest
import com.leonoretech.marianas.network.GeminiSystemInstruction
import com.leonoretech.marianas.network.OpenAIChatRequest
import com.leonoretech.marianas.network.OpenAIContentPart
import com.leonoretech.marianas.network.OpenAIImageUrl
import com.leonoretech.marianas.network.OpenAIMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles all outbound model calls. Mirrors the logic that lived in
 * buildRequest() / toOpenAIMessages() / toGeminiContents() / callModel()
 * in the original web app — same behavior, different transport.
 */
class ChatRepository {

    private val gson = Gson()

    /**
     * Sends [messages] to whichever provider is active in [config].
     * For built-in providers (OpenRouter/Groq) the wire format is fixed.
     * For Google it's always Gemini-native. For custom slots, the format
     * is whatever the user picked per-slot (OpenAI-style or Gemini-style).
     */
    fun sendMessage(config: ProviderConfig, messages: List<ChatMessage>): Flow<String> {
        val usesGeminiFormat = when (config.activeProvider) {
            Provider.GOOGLE -> true
            Provider.CUSTOM1, Provider.CUSTOM2, Provider.CUSTOM3 ->
                config.activeCustomSlot()?.formatStyle == FormatStyle.GEMINI
            else -> false
        }
        return if (usesGeminiFormat) {
            sendToGemini(config, messages)
        } else {
            sendToOpenAICompatible(config, messages)
        }
    }

    /**
     * Tries the active provider first. If it fails (network error, non-2xx, timeout),
     * tries the next *configured* provider in fallback order: OpenRouter -> Groq -> Google -> Custom,
     * skipping whichever one was already tried and any that have no key/model set.
     *
     * IMPORTANT: Kotlin Flow is cold — building a Flow does not execute the network call,
     * only collecting it does. So we must actually collect each candidate's Flow inside the
     * try/catch to detect real failures, then re-emit everything we collected through the
     * Flow we return to the caller. This means the caller gets a normal Flow<String> as if
     * a single provider had been called directly, with retries already resolved internally.
     */
    suspend fun sendMessageWithFallback(
        config: ProviderConfig,
        messages: List<ChatMessage>
    ): Pair<Provider, Flow<String>> {
        val order = listOf(
            Provider.OPENROUTER, Provider.GROQ, Provider.GOOGLE,
            Provider.CUSTOM1, Provider.CUSTOM2, Provider.CUSTOM3
        )
        val candidates = order.filter { isProviderConfigured(config, it) }
            .sortedByDescending { it == config.activeProvider } // try the user's chosen one first

        if (candidates.isEmpty()) {
            throw ApiException("Belum ada provider yang dikonfigurasi. Isi API key + model di Konfigurasi dulu.")
        }

        var lastError: Exception? = null
        for (provider in candidates) {
            val attemptConfig = config.copy(activeProvider = provider)
            try {
                val collectedChunks = mutableListOf<String>()
                sendMessage(attemptConfig, messages).collect { chunk ->
                    collectedChunks.add(chunk)
                }
                // Collected without throwing -> this provider succeeded.
                // Re-emit the exact same sequence of chunks so the UI sees identical
                // incremental updates as if it had collected the original Flow directly.
                return provider to flow {
                    collectedChunks.forEach { emit(it) }
                }
            } catch (e: Exception) {
                lastError = e
                // try next candidate
            }
        }
        throw lastError ?: ApiException("Semua provider gagal tanpa pesan error spesifik.")
    }

    private fun isProviderConfigured(config: ProviderConfig, provider: Provider): Boolean {
        val hasModel = when (provider) {
            Provider.OPENROUTER -> config.openrouterModel.isNotBlank()
            Provider.GROQ -> config.groqModel.isNotBlank()
            Provider.GOOGLE -> config.googleModel.isNotBlank()
            Provider.CUSTOM1 -> config.custom1.model.isNotBlank() && config.custom1.url.isNotBlank()
            Provider.CUSTOM2 -> config.custom2.model.isNotBlank() && config.custom2.url.isNotBlank()
            Provider.CUSTOM3 -> config.custom3.model.isNotBlank() && config.custom3.url.isNotBlank()
        }
        val hasAuth = when (provider) {
            Provider.OPENROUTER -> config.openrouterKey.isNotBlank()
            Provider.GROQ -> config.groqKey.isNotBlank()
            Provider.GOOGLE -> config.googleKey.isNotBlank()
            Provider.CUSTOM1, Provider.CUSTOM2, Provider.CUSTOM3 -> true // key optional for custom slots
        }
        return hasModel && hasAuth
    }

    // ---------------------------------------------------------------------
    // OpenAI-compatible path (OpenRouter / Groq / Custom)
    // ---------------------------------------------------------------------

    private fun sendToOpenAICompatible(config: ProviderConfig, messages: List<ChatMessage>): Flow<String> = flow {
        val service = ApiClientFactory.buildOpenAIService(config.timeoutSeconds)

        val (url, headers, model) = when (config.activeProvider) {
            Provider.OPENROUTER -> Triple(
                "https://openrouter.ai/api/v1/chat/completions",
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer ${config.openrouterKey}",
                    "X-Title" to "Marianas AI"
                ),
                config.openrouterModel
            )
            Provider.GROQ -> Triple(
                "https://api.groq.com/openai/v1/chat/completions",
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer ${config.groqKey}"
                ),
                config.groqModel
            )
            Provider.CUSTOM1, Provider.CUSTOM2, Provider.CUSTOM3 -> {
                val slot = config.activeCustomSlot()
                    ?: throw ApiException("Slot custom tidak ditemukan")
                val h = mutableMapOf("Content-Type" to "application/json")
                if (slot.key.isNotBlank()) {
                    h["Authorization"] = "Bearer ${slot.key}"
                }
                Triple(slot.url, h, slot.model)
            }
            Provider.GOOGLE -> error("sendToOpenAICompatible should never be called for GOOGLE provider")
        }

        val openAIMessages = toOpenAIMessages(messages)
        val request = OpenAIChatRequest(model = model, messages = openAIMessages, stream = config.streamEnabled)

        if (config.streamEnabled) {
            val response = service.chatCompletionStream(url, headers, request)
            if (!response.isSuccessful) {
                throw ApiException(parseOpenAIErrorBody(response))
            }
            val body = response.body() ?: throw ApiException("Empty response body")
            emitOpenAISseChunks(body, this)
        } else {
            val response = service.chatCompletion(url, headers, request)
            if (!response.isSuccessful) {
                throw ApiException(parseOpenAIErrorBody(response))
            }
            val text = response.body()?.choices?.firstOrNull()?.message?.content
                ?: throw ApiException("Respons kosong dari server")
            emit(text)
        }
    }

    private fun toOpenAIMessages(messages: List<ChatMessage>): List<OpenAIMessage> {
        return messages.map { msg ->
            val roleStr = when (msg.role) {
                Role.SYSTEM -> "system"
                Role.USER -> "user"
                Role.ASSISTANT -> "assistant"
            }
            if (msg.images.isEmpty()) {
                OpenAIMessage(role = roleStr, content = msg.content)
            } else {
                val parts = mutableListOf<OpenAIContentPart>()
                if (msg.content.isNotBlank()) {
                    parts.add(OpenAIContentPart(type = "text", text = msg.content))
                }
                msg.images.forEach { img ->
                    val dataUri = "data:${img.mimeType};base64,${img.base64Data}"
                    parts.add(OpenAIContentPart(type = "image_url", imageUrl = OpenAIImageUrl(url = dataUri)))
                }
                OpenAIMessage(role = roleStr, content = parts)
            }
        }
    }

    /**
     * Parses Server-Sent Events from an OpenAI-style streaming response.
     * Each SSE line looks like: "data: {...json...}" terminated by "data: [DONE]".
     */
    private suspend fun emitOpenAISseChunks(
        body: ResponseBody,
        collector: kotlinx.coroutines.flow.FlowCollector<String>
    ) {
        val reader = BufferedReader(InputStreamReader(body.byteStream()))
        var accumulated = ""
        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (!line.startsWith("data:")) continue
                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]") break
                if (payload.isEmpty()) continue
                try {
                    val json = JsonParser.parseString(payload).asJsonObject
                    val delta = json.getAsJsonArray("choices")
                        ?.firstOrNull()?.asJsonObject
                        ?.getAsJsonObject("delta")
                        ?.get("content")?.asString
                    if (!delta.isNullOrEmpty()) {
                        accumulated += delta
                        collector.emit(accumulated)
                    }
                } catch (e: Exception) {
                    // malformed partial chunk — ignore, matches web app behavior
                }
            }
        }
    }

    private fun parseOpenAIErrorBody(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            val msg = json.getAsJsonObject("error")?.get("message")?.asString
            "HTTP ${response.code()} — ${msg ?: errorBody}"
        } catch (e: Exception) {
            "HTTP ${response.code()} — ${errorBody?.take(200)}"
        }
    }

    // ---------------------------------------------------------------------
    // Gemini native path (Google AI)
    // ---------------------------------------------------------------------

    private fun sendToGemini(config: ProviderConfig, messages: List<ChatMessage>): Flow<String> = flow {
        val service = ApiClientFactory.buildGeminiService(config.timeoutSeconds)

        // Resolve key/model/base-endpoint depending on whether this is the built-in
        // Google AI provider (fixed generativelanguage.googleapis.com endpoint) or a
        // custom slot configured with Gemini-style format (user-supplied base URL).
        val (apiKey, model, baseUrl) = when (config.activeProvider) {
            Provider.GOOGLE -> Triple(
                config.googleKey,
                config.googleModel.ifBlank { "gemini-3.5-flash" },
                "https://generativelanguage.googleapis.com/v1beta/models"
            )
            Provider.CUSTOM1, Provider.CUSTOM2, Provider.CUSTOM3 -> {
                val slot = config.activeCustomSlot()
                    ?: throw ApiException("Slot custom tidak ditemukan")
                // For a custom Gemini-format slot, slot.url is expected to be the base
                // endpoint up to (but not including) "/models/{model}:generateContent",
                // e.g. "https://your-gemini-proxy.example.com/v1beta/models"
                Triple(slot.key, slot.model, slot.url.trimEnd('/'))
            }
            else -> error("sendToGemini should only be called for GOOGLE or a Gemini-format custom slot")
        }

        val contents = messages
            .filter { it.role != Role.SYSTEM }
            .map { msg ->
                val parts = mutableListOf<GeminiPart>()
                if (msg.content.isNotBlank()) parts.add(GeminiPart(text = msg.content))
                msg.images.forEach { img ->
                    parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = img.mimeType, data = img.base64Data)))
                }
                GeminiContent(
                    role = if (msg.role == Role.ASSISTANT) "model" else "user",
                    parts = parts.ifEmpty { listOf(GeminiPart(text = "")) }
                )
            }

        val systemInstruction = messages.firstOrNull { it.role == Role.SYSTEM }?.let {
            GeminiSystemInstruction(parts = listOf(GeminiPart(text = it.content)))
        }

        val request = GeminiRequest(contents = contents, systemInstruction = systemInstruction)

        if (config.streamEnabled) {
            val url = "$baseUrl/$model:streamGenerateContent?alt=sse"
            val response = service.streamGenerateContent(url, apiKey, request)
            if (!response.isSuccessful) {
                throw ApiException(parseGeminiErrorBody(response))
            }
            val body = response.body() ?: throw ApiException("Empty response body")
            emitGeminiSseChunks(body, this)
        } else {
            val url = "$baseUrl/$model:generateContent"
            val response = service.generateContent(url, apiKey, request)
            if (!response.isSuccessful) {
                throw ApiException(parseGeminiErrorBody(response))
            }
            val text = extractGeminiText(response.body())
                ?: throw ApiException("Respons kosong dari server")
            emit(text)
        }
    }

    private fun extractGeminiText(response: com.leonoretech.marianas.network.GeminiResponse?): String? {
        val parts = response?.candidates?.firstOrNull()?.content?.parts ?: return null
        return parts.filter { it.thought != true && it.text != null }
            .joinToString("") { it.text ?: "" }
            .ifEmpty { null }
    }

    private suspend fun emitGeminiSseChunks(
        body: ResponseBody,
        collector: kotlinx.coroutines.flow.FlowCollector<String>
    ) {
        val reader = BufferedReader(InputStreamReader(body.byteStream()))
        var accumulated = ""
        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (!line.startsWith("data:")) continue
                val payload = line.removePrefix("data:").trim()
                if (payload.isEmpty()) continue
                try {
                    val json = JsonParser.parseString(payload).asJsonObject
                    val partsArray = json.getAsJsonArray("candidates")
                        ?.firstOrNull()?.asJsonObject
                        ?.getAsJsonObject("content")
                        ?.getAsJsonArray("parts")
                    val delta = partsArray
                        ?.mapNotNull { p ->
                            val obj = p.asJsonObject
                            val isThought = obj.get("thought")?.asBoolean == true
                            if (!isThought) obj.get("text")?.asString else null
                        }
                        ?.joinToString("") ?: ""
                    if (delta.isNotEmpty()) {
                        accumulated += delta
                        collector.emit(accumulated)
                    }
                } catch (e: Exception) {
                    // malformed partial chunk — ignore
                }
            }
        }
    }

    private fun parseGeminiErrorBody(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            val msg = json.getAsJsonObject("error")?.get("message")?.asString
            msg ?: "HTTP ${response.code()}"
        } catch (e: Exception) {
            "HTTP ${response.code()} — ${errorBody?.take(200)}"
        }
    }
}

class ApiException(message: String) : Exception(message)
