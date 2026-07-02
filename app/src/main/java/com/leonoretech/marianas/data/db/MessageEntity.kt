package com.leonoretech.marianas.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single chat message (either from the user or the assistant).
 * Equivalent to the "messages" object store in the web app's IndexedDB.
 *
 * Images are stored as a JSON-encoded list of local file paths (see ImageStore),
 * not as base64 strings — Android has real filesystem access, so we avoid
 * bloating the database with base64 blobs the way the browser version had to.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "system" | "user" | "assistant"
    val content: String,
    val isError: Boolean = false,
    /** JSON array of local file URIs/paths for any attached images, e.g. ["file:///.../img1.jpg"] */
    val imagePathsJson: String = "[]",
    val timestamp: Long
)
