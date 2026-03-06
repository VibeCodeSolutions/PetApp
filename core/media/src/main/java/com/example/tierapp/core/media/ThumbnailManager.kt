package com.example.tierapp.core.media

import android.net.Uri

interface ThumbnailManager {
    data class ThumbnailResult(
        val thumbSmallPath: String,
        val thumbMediumPath: String,
    )

    fun generateThumbs(sourceUri: Uri): ThumbnailResult
}
