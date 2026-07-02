package com.leonoretech.marianas.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Google Gemini native API service.
 * Auth uses the "x-goog-api-key" header (NOT a Bearer token), per Gemini API spec.
 */
interface GeminiApiService {

    @POST
    suspend fun generateContent(
        @Url url: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    @Streaming
    @POST
    suspend fun streamGenerateContent(
        @Url url: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<ResponseBody>
}
