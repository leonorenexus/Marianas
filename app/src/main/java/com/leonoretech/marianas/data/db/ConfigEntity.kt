package com.leonoretech.marianas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding the app's provider configuration.
 * Equivalent to the "config" object store (key="app_config") in the web app's IndexedDB.
 *
 * We flatten all provider fields into one row rather than using a JSON blob,
 * so Room can validate/migrate the schema properly as fields are added later.
 */
@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 0, // always 0 — single row

    val activeProvider: String = "openrouter", // "openrouter" | "groq" | "google" | "custom1" | "custom2" | "custom3"

    val openrouterKey: String = "",
    val openrouterModel: String = "",

    val groqKey: String = "",
    val groqModel: String = "",

    val googleKey: String = "",
    val googleModel: String = "gemini-3.5-flash",

    // Three independent custom provider slots. Each can be configured as
    // either OpenAI-compatible (messages[] format) or Gemini-native
    // (contents[] format), chosen per-slot via `formatStyle`.
    val custom1Name: String = "Custom 1",
    val custom1Url: String = "",
    val custom1Key: String = "",
    val custom1Model: String = "",
    val custom1FormatStyle: String = "openai", // "openai" | "gemini"

    val custom2Name: String = "Custom 2",
    val custom2Url: String = "",
    val custom2Key: String = "",
    val custom2Model: String = "",
    val custom2FormatStyle: String = "openai",

    val custom3Name: String = "Custom 3",
    val custom3Url: String = "",
    val custom3Key: String = "",
    val custom3Model: String = "",
    val custom3FormatStyle: String = "openai",

    val systemPrompt: String = "",
    val streamEnabled: Boolean = true,
    val timeoutSeconds: Int = 300,
    val fallbackEnabled: Boolean = true
)
