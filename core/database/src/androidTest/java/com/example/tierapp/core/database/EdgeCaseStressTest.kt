package com.example.tierapp.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.PetEntity
import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/**
 * Integrationstests fuer Edge-Cases mit grossen Datenmengen.
 *
 * Verwendet In-Memory Room-Datenbank — keine Firebase-Verbindung noetig.
 */
@RunWith(AndroidJUnit4::class)
class EdgeCaseStressTest {

    private lateinit var db: TierappDatabase
    private lateinit var petDao: PetDao
    private lateinit var petPhotoDao: PetPhotoDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TierappDatabase::class.java,
        ).allowMainThreadQueries().build()
        petDao = db.petDao()
        petPhotoDao = db.petPhotoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // 1. Grosse Tierliste: 50+ Tiere
    // -------------------------------------------------------------------------

    @Test
    fun insert_55_pets_and_getAll_returns_55_ordered_by_name() = runTest {
        val count = 55
        val pets = List(count) { index ->
            fakePetEntity(id = "pet_$index", name = "Tier ${index.toString().padStart(3, '0')}")
        }
        pets.forEach { petDao.insert(it) }

        val result = petDao.getAll().first()

        assertEquals(count, result.size)
        // Sicherstellen, dass Sortierung by name stimmt
        val names = result.map { it.name }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun soft_delete_one_of_55_pets_reduces_visible_count() = runTest {
        val count = 55
        List(count) { index ->
            fakePetEntity(id = "pet_$index", name = "Tier $index")
        }.forEach { petDao.insert(it) }

        petDao.softDelete("pet_0", Instant.now().toEpochMilli())

        val result = petDao.getAll().first()
        assertEquals(count - 1, result.size)
        assertTrue(result.none { it.id == "pet_0" })
    }

    @Test
    fun insert_55_pets_all_pending_returns_55_in_getPending() = runTest {
        val count = 55
        List(count) { index ->
            fakePetEntity(
                id = "pet_$index",
                name = "Tier $index",
                syncStatus = SyncStatus.PENDING,
            )
        }.forEach { petDao.insert(it) }

        val pending = petDao.getPending()
        assertEquals(count, pending.size)
    }

    // -------------------------------------------------------------------------
    // 2. Grosse Fotoliste: 100+ Fotos pro Tier
    // -------------------------------------------------------------------------

    @Test
    fun insert_120_photos_for_one_pet_getByPetId_returns_120() = runTest {
        val pet = fakePetEntity(id = "pet_main", name = "Fotostar")
        petDao.insert(pet)

        val photoCount = 120
        List(photoCount) { index ->
            fakePetPhotoEntity(id = "photo_$index", petId = "pet_main")
        }.forEach { petPhotoDao.insert(it) }

        val result = petPhotoDao.getByPetId("pet_main").first()
        assertEquals(photoCount, result.size)
    }

    @Test
    fun getPhotosNeedingUpload_respects_limit_of_200_with_250_candidates() = runTest {
        val pet = fakePetEntity(id = "pet_main", name = "Upload-Test")
        petDao.insert(pet)

        // 250 Fotos mit LOCAL_ONLY einfuegen — LIMIT 200 greift
        List(250) { index ->
            fakePetPhotoEntity(
                id = "photo_$index",
                petId = "pet_main",
                uploadStatus = UploadStatus.LOCAL_ONLY,
                createdAt = Instant.ofEpochMilli(index.toLong()),
            )
        }.forEach { petPhotoDao.insert(it) }

        val result = petPhotoDao.getPhotosNeedingUpload()
        assertEquals(200, result.size)
    }

    @Test
    fun photos_sorted_by_createdAt_desc_in_getByPetId() = runTest {
        val pet = fakePetEntity(id = "pet_sort", name = "Sortiertest")
        petDao.insert(pet)

        val now = Instant.now()
        List(10) { index ->
            fakePetPhotoEntity(
                id = "photo_$index",
                petId = "pet_sort",
                createdAt = now.minusSeconds(index.toLong()),
            )
        }.forEach { petPhotoDao.insert(it) }

        val result = petPhotoDao.getByPetId("pet_sort").first()
        val timestamps = result.map { it.createdAt.toEpochMilli() }
        // DESC: jedes Element muss >= dem naechsten sein
        for (i in 0 until timestamps.size - 1) {
            assertTrue(
                "Sortierung verletzt bei Index $i: ${timestamps[i]} < ${timestamps[i + 1]}",
                timestamps[i] >= timestamps[i + 1],
            )
        }
    }

    // -------------------------------------------------------------------------
    // 3. Offline-Modus: Alle Operationen ohne Netz (Room-only, implizit erfuellt)
    // -------------------------------------------------------------------------

    @Test
    fun offline_insert_and_read_cycle_works_without_network() = runTest {
        // Dieser Test laeuft rein lokal (kein Firebase) und beweist,
        // dass alle CRUD-Operationen im Offline-Modus funktionieren.
        val pet = fakePetEntity(id = "offline_pet", name = "Offline-Tier")
        petDao.insert(pet)

        val loaded = petDao.getByIdDirect("offline_pet")
        assertEquals("offline_pet", loaded?.id)
        assertEquals(SyncStatus.PENDING, loaded?.syncStatus)
    }

    @Test
    fun upsert_overwrites_existing_pet() = runTest {
        val original = fakePetEntity(id = "upsert_pet", name = "Alt")
        petDao.insert(original)

        val updated = original.copy(name = "Neu", syncStatus = SyncStatus.SYNCED)
        petDao.upsert(updated)

        val result = petDao.getByIdDirect("upsert_pet")
        assertEquals("Neu", result?.name)
        assertEquals(SyncStatus.SYNCED, result?.syncStatus)
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private fun fakePetEntity(
        id: String,
        name: String,
        syncStatus: SyncStatus = SyncStatus.PENDING,
    ) = PetEntity(
        id = id,
        name = name,
        birthDate = LocalDate.of(2020, 1, 1),
        species = PetSpecies.DOG,
        breed = null,
        chipNumber = null,
        color = null,
        weightKg = null,
        notes = null,
        profilePhotoId = null,
        familyId = null,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        syncStatus = syncStatus,
        isDeleted = false,
    )

    private fun fakePetPhotoEntity(
        id: String,
        petId: String,
        uploadStatus: UploadStatus = UploadStatus.LOCAL_ONLY,
        createdAt: Instant = Instant.now(),
    ) = PetPhotoEntity(
        id = id,
        petId = petId,
        originalPath = "/storage/emulated/0/DCIM/$id.jpg",
        thumbSmallPath = null,
        thumbMediumPath = null,
        remoteOriginalUrl = null,
        remoteThumbSmallUrl = null,
        remoteThumbMediumUrl = null,
        uploadStatus = uploadStatus,
        createdAt = createdAt,
        updatedAt = createdAt,
        syncStatus = SyncStatus.PENDING,
        isDeleted = false,
    )
}
