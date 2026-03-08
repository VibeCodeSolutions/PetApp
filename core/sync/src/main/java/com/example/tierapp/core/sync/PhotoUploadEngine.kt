package com.example.tierapp.core.sync

import android.util.Log
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.model.UploadStatus
import com.example.tierapp.core.network.storage.StorageDataSource
import java.io.File
import javax.inject.Inject

class PhotoUploadEngine @Inject constructor(
    private val petPhotoDao: PetPhotoDao,
    private val storageDataSource: StorageDataSource,
) {

    /**
     * Laedt alle LOCAL_ONLY/FAILED Fotos hoch (max. 200 pro Aufruf, siehe PetPhotoDao.getPhotosNeedingUpload).
     * Bricht fruehzeitig ab, wenn [MAX_CONSECUTIVE_FAILURES] aufeinanderfolgende Fehler auftreten,
     * um bei Netzwerkausfall keinen langen Retry-Loop zu starten.
     *
     * @return true wenn alle erfolgreich, false wenn mindestens eines fehlgeschlagen
     */
    suspend fun uploadPending(familyId: String): Boolean {
        val photos = petPhotoDao.getPhotosNeedingUpload()
        if (photos.isEmpty()) return true

        var allSuccess = true
        var consecutiveFailures = 0
        for (photo in photos) {
            val success = uploadSingle(familyId, photo)
            if (success) {
                consecutiveFailures = 0
            } else {
                allSuccess = false
                consecutiveFailures++
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    Log.w(TAG, "Early exit: $consecutiveFailures consecutive upload failures")
                    break
                }
            }
        }
        return allSuccess
    }

    private suspend fun uploadSingle(familyId: String, photo: PetPhotoEntity): Boolean {
        return try {
            petPhotoDao.updateUploadStatus(photo.id, UploadStatus.UPLOADING)

            val originalFile = File(photo.originalPath)
            val thumbSmallFile = photo.thumbSmallPath?.let { File(it) }
            val thumbMediumFile = photo.thumbMediumPath?.let { File(it) }

            val urls = storageDataSource.uploadPhoto(
                familyId = familyId,
                petId = photo.petId,
                photoId = photo.id,
                originalFile = originalFile,
                thumbSmallFile = thumbSmallFile,
                thumbMediumFile = thumbMediumFile,
            )

            petPhotoDao.updateRemoteUrls(
                id = photo.id,
                originalUrl = urls.originalUrl,
                thumbSmallUrl = urls.thumbSmallUrl,
                thumbMediumUrl = urls.thumbMediumUrl,
            )

            Log.d(TAG, "Photo ${photo.id} uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for photo ${photo.id}", e)
            petPhotoDao.updateUploadStatus(photo.id, UploadStatus.FAILED)
            false
        }
    }

    companion object {
        private const val TAG = "PhotoUploadEngine"
        const val MAX_CONSECUTIVE_FAILURES = 2
    }
}
