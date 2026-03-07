package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.network.storage.PhotoUrls
import com.example.tierapp.core.network.storage.StorageDataSource
import java.io.File

class FakeStorageDataSource : StorageDataSource {

    var shouldThrow = false
    var uploadCount = 0
    var onUploadCapture: (() -> Unit)? = null

    override suspend fun uploadPhoto(
        familyId: String,
        petId: String,
        photoId: String,
        originalFile: File,
        thumbSmallFile: File?,
        thumbMediumFile: File?,
    ): PhotoUrls {
        onUploadCapture?.invoke()
        if (shouldThrow) throw RuntimeException("Storage error")
        uploadCount++
        return PhotoUrls(
            originalUrl = "https://storage.example.com/$familyId/$petId/$photoId/original.jpg",
            thumbSmallUrl = thumbSmallFile?.let {
                "https://storage.example.com/$familyId/$petId/$photoId/thumb_s.jpg"
            },
            thumbMediumUrl = thumbMediumFile?.let {
                "https://storage.example.com/$familyId/$petId/$photoId/thumb_m.jpg"
            },
        )
    }
}
