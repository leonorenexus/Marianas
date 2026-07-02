package com.leonoretech.marianas.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Handles persisting picked images into app-private storage and converting
 * them to base64 for sending to AI providers.
 *
 * Why copy into app-private storage at all (instead of just keeping the
 * picked content:// Uri)? Photo Picker grants only a *temporary* read
 * permission to the original Uri — it is not guaranteed to remain valid
 * after the app process dies (e.g. user closes the app, opens it days later).
 * Copying the bytes into our own files dir guarantees the image is still
 * there when the user reopens a past chat session.
 */
class ImageStore(private val context: Context) {

    private val imagesDir: File by lazy {
        File(context.filesDir, "chat_images").apply { mkdirs() }
    }

    /**
     * Copies the picked image into app-private storage and returns the
     * absolute file path (stored later as JSON in MessageEntity.imagePathsJson).
     */
    fun persistPickedImage(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when {
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val fileName = "${UUID.randomUUID()}.$extension"
        val destFile = File(imagesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Tidak bisa membaca gambar yang dipilih")

        return destFile.absolutePath
    }

    /** Reads a previously persisted image file and base64-encodes it for an API request. */
    fun toAttachedImage(filePath: String): AttachedImage? {
        val file = File(filePath)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val mimeType = guessMimeType(file.extension)
        return AttachedImage(base64Data = base64, mimeType = mimeType)
    }

    private fun guessMimeType(extension: String): String = when (extension.lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }

    /** Deletes a previously persisted image file (used when user removes an attached preview). */
    fun delete(filePath: String) {
        File(filePath).takeIf { it.exists() }?.delete()
    }

    fun fileSizeBytes(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) file.length() else 0L
    }
}
