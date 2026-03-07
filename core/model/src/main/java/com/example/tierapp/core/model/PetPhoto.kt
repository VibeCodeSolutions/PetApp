package com.example.tierapp.core.model

import java.time.Instant

data class PetPhoto(
    val id: String,
    val petId: String,
    val originalPath: String,
    val thumbSmallPath: String?,
    val thumbMediumPath: String?,
    val remoteOriginalUrl: String? = null,
    val remoteThumbSmallUrl: String? = null,
    val remoteThumbMediumUrl: String? = null,
    val uploadStatus: UploadStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)
