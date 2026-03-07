package com.example.tierapp.core.sync

import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import com.example.tierapp.core.sync.fake.FakeFirestoreDataSource
import com.example.tierapp.core.sync.fake.FakePetDao
import com.example.tierapp.core.sync.fake.FakePetPhotoDao
import com.example.tierapp.core.sync.fake.FakeSyncPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SyncEngineTest {

    private lateinit var petDao: FakePetDao
    private lateinit var photoDao: FakePetPhotoDao
    private lateinit var firestoreDataSource: FakeFirestoreDataSource
    private lateinit var syncPrefs: FakeSyncPreferences
    private lateinit var engine: SyncEngine

    private val familyId = "test-family-id"

    @Before
    fun setup() {
        petDao = FakePetDao()
        photoDao = FakePetPhotoDao()
        firestoreDataSource = FakeFirestoreDataSource()
        syncPrefs = FakeSyncPreferences()
        engine = SyncEngine(
            petDao = petDao,
            petPhotoDao = photoDao,
            firestoreDataSource = firestoreDataSource,
            syncResolver = SyncResolver(),
            syncPreferences = syncPrefs,
        )
    }

    @Test
    fun `push phase - pending pets are pushed to Firestore and marked SYNCED`() = runTest {
        petDao.insertForTest(createPet(id = "p1", syncStatus = SyncStatus.PENDING))

        engine.sync(familyId)

        assertEquals(SyncStatus.SYNCED, petDao.getDomainById("p1")?.syncStatus)
        assertEquals(1, firestoreDataSource.pushedPets.size)
    }

    @Test
    fun `push phase - pending photos are pushed to Firestore and marked SYNCED`() = runTest {
        photoDao.insertForTest(createPhoto(id = "ph1", syncStatus = SyncStatus.PENDING))

        engine.sync(familyId)

        assertEquals(SyncStatus.SYNCED, photoDao.getDomainById("ph1")?.syncStatus)
        assertEquals(1, firestoreDataSource.pushedPhotos.size)
    }

    @Test
    fun `push phase - non-pending entities are not pushed`() = runTest {
        petDao.insertForTest(createPet(id = "p1", syncStatus = SyncStatus.SYNCED))

        engine.sync(familyId)

        assertTrue(firestoreDataSource.pushedPets.isEmpty())
    }

    @Test
    fun `pull phase - new remote pet is inserted locally`() = runTest {
        firestoreDataSource.remotePets.add(
            createPet(id = "p-remote", syncStatus = SyncStatus.SYNCED, updatedAtMillis = 5000L)
        )

        engine.sync(familyId)

        val localPet = petDao.getDomainById("p-remote")
        assertEquals("p-remote", localPet?.id)
        assertEquals(SyncStatus.SYNCED, localPet?.syncStatus)
    }

    @Test
    fun `pull phase - remote pet with newer timestamp overwrites SYNCED local`() = runTest {
        petDao.insertForTest(
            createPet(id = "p1", name = "Local", syncStatus = SyncStatus.SYNCED, updatedAtMillis = 1000L)
        )
        firestoreDataSource.remotePets.add(
            createPet(id = "p1", name = "Remote", syncStatus = SyncStatus.SYNCED, updatedAtMillis = 2000L)
        )

        engine.sync(familyId)

        assertEquals("Remote", petDao.getDomainById("p1")?.name)
    }

    @Test
    fun `pull phase - PENDING local with newer timestamp is preserved and then pushed`() = runTest {
        petDao.insertForTest(
            createPet(id = "p1", name = "Local", syncStatus = SyncStatus.PENDING, updatedAtMillis = 3000L)
        )
        firestoreDataSource.remotePets.add(
            createPet(id = "p1", name = "Remote", syncStatus = SyncStatus.SYNCED, updatedAtMillis = 2000L)
        )

        engine.sync(familyId)

        // Local name preserved (local wins conflict)
        assertEquals("Local", petDao.getDomainById("p1")?.name)
        // After full sync: pull preserved local, push then synced it
        assertEquals(SyncStatus.SYNCED, petDao.getDomainById("p1")?.syncStatus)
        // The local version was pushed to Firestore
        assertEquals(1, firestoreDataSource.pushedPets.size)
        assertEquals("Local", firestoreDataSource.pushedPets[0].name)
    }

    @Test
    fun `pull phase - remote deletion is applied locally`() = runTest {
        petDao.insertForTest(
            createPet(id = "p1", syncStatus = SyncStatus.SYNCED, isDeleted = false)
        )
        firestoreDataSource.remotePets.add(
            createPet(id = "p1", syncStatus = SyncStatus.SYNCED, isDeleted = true)
        )

        engine.sync(familyId)

        assertTrue(petDao.getDomainById("p1")?.isDeleted == true)
    }

    @Test
    fun `sync updates lastSyncTimestamp`() = runTest {
        engine.sync(familyId)
        assertTrue(syncPrefs.lastSyncTimestamp > 0)
    }

    @Test
    fun `sync with Firestore error does not crash - returns false`() = runTest {
        firestoreDataSource.shouldThrow = true
        val result = engine.sync(familyId)
        assertEquals(false, result)
    }

    // --- Helper ---

    private fun createPet(
        id: String = "pet-1",
        name: String = "Bello",
        syncStatus: SyncStatus = SyncStatus.PENDING,
        updatedAtMillis: Long = 1000L,
        isDeleted: Boolean = false,
    ) = Pet(
        id = id,
        name = name,
        birthDate = null,
        species = PetSpecies.DOG,
        breed = null,
        chipNumber = null,
        color = null,
        weightKg = null,
        notes = null,
        profilePhotoId = null,
        familyId = familyId,
        createdAt = Instant.ofEpochMilli(1000L),
        updatedAt = Instant.ofEpochMilli(updatedAtMillis),
        syncStatus = syncStatus,
        isDeleted = isDeleted,
    )

    private fun createPhoto(
        id: String = "photo-1",
        petId: String = "pet-1",
        syncStatus: SyncStatus = SyncStatus.PENDING,
        updatedAtMillis: Long = 1000L,
    ) = PetPhoto(
        id = id,
        petId = petId,
        originalPath = "/path/to/original.jpg",
        thumbSmallPath = "/path/to/thumb_s.jpg",
        thumbMediumPath = "/path/to/thumb_m.jpg",
        remoteOriginalUrl = null,
        remoteThumbSmallUrl = null,
        remoteThumbMediumUrl = null,
        uploadStatus = UploadStatus.LOCAL_ONLY,
        createdAt = Instant.ofEpochMilli(1000L),
        updatedAt = Instant.ofEpochMilli(updatedAtMillis),
        syncStatus = syncStatus,
        isDeleted = false,
    )
}
