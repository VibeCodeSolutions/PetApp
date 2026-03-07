package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import java.time.Instant

@Entity(
    tableName = "pet_photo",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("petId")],
)
data class PetPhotoEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val originalPath: String,
    val thumbSmallPath: String?,
    val thumbMediumPath: String?,
    val remoteOriginalUrl: String?,
    val remoteThumbSmallUrl: String?,
    val remoteThumbMediumUrl: String?,
    val uploadStatus: UploadStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)

fun PetPhotoEntity.toDomain(): PetPhoto = PetPhoto(
    id = id,
    petId = petId,
    originalPath = originalPath,
    thumbSmallPath = thumbSmallPath,
    thumbMediumPath = thumbMediumPath,
    remoteOriginalUrl = remoteOriginalUrl,
    remoteThumbSmallUrl = remoteThumbSmallUrl,
    remoteThumbMediumUrl = remoteThumbMediumUrl,
    uploadStatus = uploadStatus,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun PetPhoto.toEntity(): PetPhotoEntity = PetPhotoEntity(
    id = id,
    petId = petId,
    originalPath = originalPath,
    thumbSmallPath = thumbSmallPath,
    thumbMediumPath = thumbMediumPath,
    remoteOriginalUrl = remoteOriginalUrl,
    remoteThumbSmallUrl = remoteThumbSmallUrl,
    remoteThumbMediumUrl = remoteThumbMediumUrl,
    uploadStatus = uploadStatus,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
