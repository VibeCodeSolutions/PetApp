package com.example.tierapp.core.sync

import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import com.example.tierapp.core.sync.fake.FakePetPhotoDao
import com.example.tierapp.core.sync.fake.FakeStorageDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class PhotoUploadEngineTest {

    private lateinit var photoDao: FakePetPhotoDao
    private lateinit var storageDataSource: FakeStorageDataSource
    private lateinit var engine: PhotoUploadEngine

    private val familyId = "test-family-id"

    @Before
    fun setup() {
        photoDao = FakePetPhotoDao()
        storageDataSource = FakeStorageDataSource()
        engine = PhotoUploadEngine(
            petPhotoDao = photoDao,
            storageDataSource = storageDataSource,
        )
    }

    @Test
    fun `uploads LOCAL_ONLY photos successfully`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.LOCAL_ONLY))

        val result = engine.uploadPending(familyId)

        assertTrue(result)
        assertEquals(UploadStatus.UPLOADED, photoDao.getDomainById("ph1")?.uploadStatus)
    }

    @Test
    fun `uploads FAILED photos on retry`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.FAILED))

        val result = engine.uploadPending(familyId)

        assertTrue(result)
        assertEquals(UploadStatus.UPLOADED, photoDao.getDomainById("ph1")?.uploadStatus)
    }

    @Test
    fun `sets UPLOADING status during upload`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.LOCAL_ONLY))
        storageDataSource.onUploadCapture = {
            assertEquals(UploadStatus.UPLOADING, photoDao.getDomainById("ph1")?.uploadStatus)
        }

        engine.uploadPending(familyId)
    }

    @Test
    fun `stores remote URLs after successful upload`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.LOCAL_ONLY))

        engine.uploadPending(familyId)

        val updated = photoDao.getDomainById("ph1")!!
        assertTrue(updated.remoteOriginalUrl?.contains("original") == true)
    }

    @Test
    fun `marks FAILED on storage error`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.LOCAL_ONLY))
        storageDataSource.shouldThrow = true

        val result = engine.uploadPending(familyId)

        assertEquals(false, result)
        assertEquals(UploadStatus.FAILED, photoDao.getDomainById("ph1")?.uploadStatus)
    }

    @Test
    fun `skips UPLOADED photos`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.UPLOADED))

        val result = engine.uploadPending(familyId)

        assertTrue(result)
        assertEquals(0, storageDataSource.uploadCount)
    }

    @Test
    fun `skips deleted photos`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", uploadStatus = UploadStatus.LOCAL_ONLY, isDeleted = true))

        val result = engine.uploadPending(familyId)

        assertTrue(result)
        assertEquals(0, storageDataSource.uploadCount)
    }

    // --- Helper ---

    private fun createPhoto(
        id: String = "photo-1",
        petId: String = "pet-1",
        uploadStatus: UploadStatus = UploadStatus.LOCAL_ONLY,
        isDeleted: Boolean = false,
    ) = PetPhoto(
        id = id,
        petId = petId,
        originalPath = "/path/to/original.jpg",
        thumbSmallPath = "/path/to/thumb_s.jpg",
        thumbMediumPath = "/path/to/thumb_m.jpg",
        remoteOriginalUrl = null,
        remoteThumbSmallUrl = null,
        remoteThumbMediumUrl = null,
        uploadStatus = uploadStatus,
        createdAt = Instant.ofEpochMilli(1000L),
        updatedAt = Instant.ofEpochMilli(1000L),
        syncStatus = SyncStatus.PENDING,
        isDeleted = isDeleted,
    )
}
