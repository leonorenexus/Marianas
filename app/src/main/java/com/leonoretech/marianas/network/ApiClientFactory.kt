package com.leonoretech.marianas.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds Retrofit clients on demand with a caller-supplied timeout, since the
 * app lets the user configure response timeout (default 300s) per the original
 * web app's "model berat bisa lama" setting.
 *
 * A dummy baseUrl is required by Retrofit's builder even though every actual
 * call uses @Url with a full absolute URL — the baseUrl is never used for
 * dispatch in that case, it's just a Retrofit API requirement.
 */
object ApiClientFactory {

    private val gson: Gson = GsonBuilder().create()

    fun buildOpenAIService(timeoutSeconds: Int): OpenAIApiService {
        return retrofit(timeoutSeconds).create(OpenAIApiService::class.java)
    }

    fun buildGeminiService(timeoutSeconds: Int): GeminiApiService {
        return retrofit(timeoutSeconds).create(GeminiApiService::class.java)
    }

    private fun retrofit(timeoutSeconds: Int): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://placeholder.invalid/") // unused — every call supplies a full @Url
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
