package com.example.tierapp.core.network.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
) : StorageDataSource {

    override suspend fun uploadPhoto(
        familyId: String,
        petId: String,
        photoId: String,
        originalFile: File,
        thumbSmallFile: File?,
        thumbMediumFile: File?,
    ): PhotoUrls {
        val basePath = "families/$familyId/pets/$petId/photos/$photoId"

        val originalUrl = uploadFile("$basePath/original.jpg", originalFile)
        val thumbSmallUrl = thumbSmallFile?.let { uploadFile("$basePath/thumb_s.jpg", it) }
        val thumbMediumUrl = thumbMediumFile?.let { uploadFile("$basePath/thumb_m.jpg", it) }

        return PhotoUrls(
            originalUrl = originalUrl,
            thumbSmallUrl = thumbSmallUrl,
            thumbMediumUrl = thumbMediumUrl,
        )
    }

    private suspend fun uploadFile(path: String, file: File): String {
        val ref = storage.reference.child(path)
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }
}
