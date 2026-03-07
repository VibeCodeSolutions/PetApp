package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePetPhotoDao : PetPhotoDao {

    private val store = mutableMapOf<String, PetPhotoEntity>()
    private val flow = MutableStateFlow<List<PetPhotoEntity>>(emptyList())

    fun insertForTest(photo: PetPhoto) {
        store[photo.id] = photo.toEntity()
    }

    fun getDomainById(id: String): PetPhoto? = store[id]?.toDomain()

    override fun getByPetId(petId: String): Flow<List<PetPhotoEntity>> =
        flow.map { store.values.filter { it.petId == petId && !it.isDeleted }.toList() }

    override suspend fun insert(photo: PetPhotoEntity) {
        store[photo.id] = photo
    }

    override suspend fun softDelete(id: String) {
        store[id]?.let { store[id] = it.copy(isDeleted = true, syncStatus = SyncStatus.PENDING) }
    }

    override suspend fun getPending(): List<PetPhotoEntity> =
        store.values.filter { it.syncStatus == SyncStatus.PENDING }.toList()

    override suspend fun getByIdDirect(id: String): PetPhotoEntity? = store[id]

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        store[id]?.let { store[id] = it.copy(syncStatus = status) }
    }

    override suspend fun updateUploadStatus(id: String, status: UploadStatus) {
        store[id]?.let { store[id] = it.copy(uploadStatus = status) }
    }

    override suspend fun updateRemoteUrls(
        id: String,
        originalUrl: String,
        thumbSmallUrl: String?,
        thumbMediumUrl: String?,
    ) {
        store[id]?.let {
            store[id] = it.copy(
                remoteOriginalUrl = originalUrl,
                remoteThumbSmallUrl = thumbSmallUrl,
                remoteThumbMediumUrl = thumbMediumUrl,
                uploadStatus = UploadStatus.UPLOADED,
                syncStatus = SyncStatus.PENDING,
            )
        }
    }

    override suspend fun getPhotosNeedingUpload(): List<PetPhotoEntity> =
        store.values.filter {
            (it.uploadStatus == UploadStatus.LOCAL_ONLY || it.uploadStatus == UploadStatus.FAILED) && !it.isDeleted
        }.toList()

    override suspend fun upsert(photo: PetPhotoEntity) {
        store[photo.id] = photo
    }
}
