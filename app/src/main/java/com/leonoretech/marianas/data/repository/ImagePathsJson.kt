package com.leonoretech.marianas.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converts between List<String> (file paths) and the JSON string stored in
 * MessageEntity.imagePathsJson. Kept as a tiny standalone object so both the
 * ViewModel and ChatRepository can use it without circular dependencies.
 */
object ImagePathsJson {

    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    fun encode(paths: List<String>): String = gson.toJson(paths)

    fun decode(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            gson.fromJson(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
