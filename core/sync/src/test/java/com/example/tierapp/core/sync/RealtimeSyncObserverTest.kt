package com.example.tierapp.core.sync

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import com.example.tierapp.core.sync.fake.FakeAuthRepository
import com.example.tierapp.core.sync.fake.FakeFamilyDao
import com.example.tierapp.core.sync.fake.FakeFirestoreDataSource
import com.example.tierapp.core.sync.fake.FakePetDao
import com.example.tierapp.core.sync.fake.FakePetPhotoDao
import com.example.tierapp.core.sync.fake.FakeSyncPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import com.example.tierapp.core.database.entity.FamilyEntity
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeSyncObserverTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var petDao: FakePetDao
    private lateinit var photoDao: FakePetPhotoDao
    private lateinit var firestoreDataSource: FakeFirestoreDataSource
    private lateinit var syncPrefs: FakeSyncPreferences
    private lateinit var syncEngine: SyncEngine
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var familyDao: FakeFamilyDao
    private lateinit var observer: RealtimeSyncObserver

    private val authenticatedUser = AuthUser(
        uid = "test-uid",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    /** Einfacher LifecycleOwner-Stub für Tests — ersetzt ProcessLifecycleOwner. */
    private val lifecycleOwner = object : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = registry
    }

    @Before
    fun setup() {
        petDao = FakePetDao()
        photoDao = FakePetPhotoDao()
        firestoreDataSource = FakeFirestoreDataSource()
        syncPrefs = FakeSyncPreferences()
        syncEngine = SyncEngine(petDao, photoDao, firestoreDataSource, SyncResolver(), syncPrefs)
        authRepository = FakeAuthRepository(authenticatedUser)
        familyDao = FakeFamilyDao(
            initialFamily = FamilyEntity(
                id = "family-test",
                name = "Testfamilie",
                createdBy = authenticatedUser.uid,
                inviteCode = "TEST1234",
                createdAt = Instant.ofEpochMilli(0),
                updatedAt = Instant.ofEpochMilli(0),
            )
        )

        observer = RealtimeSyncObserver(
            firestoreDataSource = firestoreDataSource,
            syncEngine = syncEngine,
            authRepository = authRepository,
            familyDao = familyDao,
            externalScope = testScope,
        )
    }

    @Test
    fun `onStart mit eingeloggtem User startet Collection`() = testScope.runTest {
        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        val pet = createRemotePet("p1")
        firestoreDataSource.petSnapshotFlow.emit(listOf(pet))

        val local = petDao.getDomainById("p1")
        assertEquals("p1", local?.id)
        assertEquals(SyncStatus.SYNCED, local?.syncStatus)
    }

    @Test
    fun `onStart ohne User startet keinen Listener`() = testScope.runTest {
        authRepository.currentUserValue = null
        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        firestoreDataSource.petSnapshotFlow.emit(listOf(createRemotePet("p1")))

        assertEquals(null, petDao.getDomainById("p1"))
    }

    @Test
    fun `onStart ohne Familie startet keinen Listener`() = testScope.runTest {
        familyDao.setFamily(null)
        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        firestoreDataSource.petSnapshotFlow.emit(listOf(createRemotePet("p1")))

        assertEquals(null, petDao.getDomainById("p1"))
    }

    @Test
    fun `onStop bricht Collection ab - nachfolgende Snapshots werden ignoriert`() = testScope.runTest {
        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        // Ersten Snapshot empfangen -> wird gemergt
        firestoreDataSource.petSnapshotFlow.emit(listOf(createRemotePet("p1")))
        assertEquals("p1", petDao.getDomainById("p1")?.id)

        // App in Hintergrund
        lifecycleOwner.registry.currentState = Lifecycle.State.CREATED // triggers onStop

        // Zweiter Snapshot: soll nicht mehr ankommen
        firestoreDataSource.petSnapshotFlow.emit(listOf(createRemotePet("p1"), createRemotePet("p2")))
        assertEquals(null, petDao.getDomainById("p2"))
    }

    @Test
    fun `Photo-Snapshots werden korrekt in Room gemergt`() = testScope.runTest {
        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        firestoreDataSource.photoSnapshotFlow.emit(listOf(createRemotePhoto("ph1", "p1")))

        val local = photoDao.getDomainById("ph1")
        assertEquals("ph1", local?.id)
        assertEquals(SyncStatus.SYNCED, local?.syncStatus)
    }

    @Test
    fun `LWW - lokales PENDING mit neuerem Timestamp wird nicht ueberschrieben`() = testScope.runTest {
        // Lokales PENDING, neuer Timestamp
        petDao.insertForTest(createRemotePet("p1", name = "Lokal", updatedAtMillis = 5000L)
            .copy(syncStatus = SyncStatus.PENDING))

        observer.register(lifecycleOwner)
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED

        // Remote älter
        firestoreDataSource.petSnapshotFlow.emit(
            listOf(createRemotePet("p1", name = "Remote", updatedAtMillis = 1000L))
        )

        assertEquals("Lokal", petDao.getDomainById("p1")?.name)
    }

    // --- Helpers ---

    private fun createRemotePet(
        id: String,
        name: String = "Bello",
        updatedAtMillis: Long = 2000L,
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
        familyId = authenticatedUser.uid,
        createdAt = Instant.ofEpochMilli(1000L),
        updatedAt = Instant.ofEpochMilli(updatedAtMillis),
        syncStatus = SyncStatus.SYNCED,
        isDeleted = false,
    )

    private fun createRemotePhoto(id: String, petId: String) = PetPhoto(
        id = id,
        petId = petId,
        originalPath = "",
        thumbSmallPath = null,
        thumbMediumPath = null,
        remoteOriginalUrl = "https://example.com/photo.jpg",
        remoteThumbSmallUrl = null,
        remoteThumbMediumUrl = null,
        uploadStatus = UploadStatus.UPLOADED,
        createdAt = Instant.ofEpochMilli(1000L),
        updatedAt = Instant.ofEpochMilli(2000L),
        syncStatus = SyncStatus.SYNCED,
        isDeleted = false,
    )
}
