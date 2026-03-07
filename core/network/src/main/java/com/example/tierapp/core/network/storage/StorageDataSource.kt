package com.example.tierapp.core.network.storage

import java.io.File

data class PhotoUrls(
    val originalUrl: String,
    val thumbSmallUrl: String?,
    val thumbMediumUrl: String?,
)

interface StorageDataSource {

    suspend fun uploadPhoto(
        familyId: String,
        petId: String,
        photoId: String,
        originalFile: File,
        thumbSmallFile: File?,
        thumbMediumFile: File?,
    ): PhotoUrls
}
