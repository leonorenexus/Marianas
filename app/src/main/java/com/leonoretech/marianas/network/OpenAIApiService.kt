package com.leonoretech.marianas.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Generic OpenAI-compatible chat completions service.
 * Used for OpenRouter, Groq, and Custom providers — they all speak the same
 * /v1/chat/completions shape, just with different base URLs and headers.
 *
 * We use a fully dynamic @Url (rather than a fixed @baseUrl + relative path)
 * because each provider's full endpoint URL differs and Custom endpoints
 * are entirely user-defined.
 */
interface OpenAIApiService {

    @POST
    suspend fun chatCompletion(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body request: OpenAIChatRequest
    ): Response<OpenAIChatResponse>

    @Streaming
    @POST
    suspend fun chatCompletionStream(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body request: OpenAIChatRequest
    ): Response<ResponseBody>
}
