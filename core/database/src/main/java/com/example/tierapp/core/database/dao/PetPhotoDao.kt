package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PetPhotoDao {

    @Query("SELECT * FROM pet_photo WHERE petId = :petId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getByPetId(petId: String): Flow<List<PetPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PetPhotoEntity)

    @Query("UPDATE pet_photo SET isDeleted = 1, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun softDelete(id: String)

    // --- Sync-Queries ---

    @Query("SELECT * FROM pet_photo WHERE syncStatus = 'PENDING'")
    suspend fun getPending(): List<PetPhotoEntity>

    @Query("SELECT * FROM pet_photo WHERE id = :id")
    suspend fun getByIdDirect(id: String): PetPhotoEntity?

    @Query("UPDATE pet_photo SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE pet_photo SET uploadStatus = :status WHERE id = :id")
    suspend fun updateUploadStatus(id: String, status: UploadStatus)

    @Query("""
        UPDATE pet_photo SET
            remoteOriginalUrl = :originalUrl,
            remoteThumbSmallUrl = :thumbSmallUrl,
            remoteThumbMediumUrl = :thumbMediumUrl,
            uploadStatus = 'UPLOADED',
            syncStatus = 'PENDING'
        WHERE id = :id
    """)
    suspend fun updateRemoteUrls(
        id: String,
        originalUrl: String,
        thumbSmallUrl: String?,
        thumbMediumUrl: String?,
    )

    @Query("""
        SELECT * FROM pet_photo
        WHERE (uploadStatus = 'LOCAL_ONLY' OR uploadStatus = 'FAILED' OR uploadStatus = 'UPLOADING')
          AND isDeleted = 0
        ORDER BY createdAt ASC
        LIMIT 200
    """)
    suspend fun getPhotosNeedingUpload(): List<PetPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: PetPhotoEntity)
}
