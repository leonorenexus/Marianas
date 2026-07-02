package com.leonoretech.marianas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one chat session (a single conversation thread).
 * Equivalent to the "sessions" object store in the web app's IndexedDB.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long
)
