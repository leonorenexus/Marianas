package com.leonoretech.marianas.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import android.net.Uri

/**
 * Wraps Android's modern Photo Picker (PickMultipleVisualMedia) for selecting
 * up to [maxItems] images at once. This API works on API 21+ via Google Play
 * Services backport — no storage permission needed, since the picker runs in
 * a separate trusted system process and only grants the app read access to
 * the specific files the user selects.
 *
 * Returns a launcher function: call it to open the picker; selected URIs are
 * delivered via [onImagesPicked].
 */
@Composable
fun rememberImagePickerLauncher(
    maxItems: Int = 4,
    onImagesPicked: (List<Uri>) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesPicked(uris)
        }
    }

    return remember(launcher) {
        {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}
