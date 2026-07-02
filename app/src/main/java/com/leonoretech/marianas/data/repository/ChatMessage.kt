package com.leonoretech.marianas.data.repository

/**
 * Generic, provider-agnostic chat message used by the ViewModel/UI layer.
 * NetworkRepository converts this into whichever wire format the active
 * provider needs (OpenAI-style messages[] or Gemini-style contents[]).
 */
data class ChatMessage(
    val role: Role,
    val content: String,
    val images: List<AttachedImage> = emptyList()
)

enum class Role { SYSTEM, USER, ASSISTANT }

/**
 * An image attached to a message, already loaded into memory as base64
 * (without the "data:mime;base64," prefix) plus its mime type.
 * The ViewModel is responsible for reading the file and base64-encoding it
 * before constructing this — the repository only deals with ready-to-send data.
 */
data class AttachedImage(
    val base64Data: String,
    val mimeType: String
)
