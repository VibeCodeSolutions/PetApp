package com.example.tierapp.core.model

import java.time.Instant

data class PetPhoto(
    val id: String,
    val petId: String,
    val originalPath: String,
    val thumbSmallPath: String?,
    val thumbMediumPath: String?,
    val uploadStatus: UploadStatus,
    val createdAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)
