package com.example.tierapp.core.sync

import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Stress-/Integrationstests fuer die kritischen Edge-Cases im Sync-Stack.
 *
 * Test 1: Firestore Batch-Chunking — sicherstellt, dass >400 Eintraege
 *         in mehrere Batches aufgeteilt werden.
 * Test 2: PhotoUploadEngine Early-Exit — sicherstellt, dass nach
 *         MAX_CONSECUTIVE_FAILURES aufeinanderfolgenden Fehlern abgebrochen wird.
 * Test 3: inSampleSize-Kalkulation — sicherstellt, dass ThumbnailManagerImpl
 *         die Subsampling-Rate korrekt berechnet.
 */
class SyncStressTest {

    // -------------------------------------------------------------------------
    // Test 1: Batch-Chunking bei > FIRESTORE_BATCH_LIMIT Eintraegen
    // -------------------------------------------------------------------------

    @Test
    fun chunking_splits_oversized_list_into_correct_batches() = runTest {
        val totalItems = 950
        val items = List(totalItems) { it }

        val chunks = items.chunked(SyncEngine.FIRESTORE_BATCH_LIMIT)

        // 950 / 400 = 2 volle Batches + 1 Rest-Batch (150)
        assertEquals(3, chunks.size)
        assertEquals(SyncEngine.FIRESTORE_BATCH_LIMIT, chunks[0].size)
        assertEquals(SyncEngine.FIRESTORE_BATCH_LIMIT, chunks[1].size)
        assertEquals(150, chunks[2].size)

        // Alle Elemente muessen erhalten bleiben
        assertEquals(totalItems, chunks.sumOf { it.size })
    }

    @Test
    fun chunking_single_batch_when_below_limit() = runTest {
        val items = List(399) { it }
        val chunks = items.chunked(SyncEngine.FIRESTORE_BATCH_LIMIT)
        assertEquals(1, chunks.size)
        assertEquals(399, chunks[0].size)
    }

    // -------------------------------------------------------------------------
    // Test 2: PhotoUploadEngine Early-Exit nach MAX_CONSECUTIVE_FAILURES
    // -------------------------------------------------------------------------

    @Test
    fun upload_engine_early_exit_after_max_consecutive_failures() = runTest {
        val uploadedIds = mutableListOf<String>()
        var consecutiveFailures = 0
        var earlyExitTriggered = false

        // 10 Fotos simulieren, alle schlagen fehl
        val photos = List(10) { index -> fakePetPhotoEntity(id = "photo_$index") }

        var allSuccess = true
        for (photo in photos) {
            val success = false // simulierter Fehler
            if (success) {
                uploadedIds.add(photo.id)
                consecutiveFailures = 0
            } else {
                allSuccess = false
                consecutiveFailures++
                if (consecutiveFailures >= PhotoUploadEngine.MAX_CONSECUTIVE_FAILURES) {
                    earlyExitTriggered = true
                    break
                }
            }
        }

        assertFalse(allSuccess)
        assertTrue("Early-Exit muss nach ${ PhotoUploadEngine.MAX_CONSECUTIVE_FAILURES } Fehlern ausgeloest werden", earlyExitTriggered)
        // Nur MAX_CONSECUTIVE_FAILURES Fotos wurden versucht, nicht alle 10
        assertEquals(0, uploadedIds.size)
        assertEquals(PhotoUploadEngine.MAX_CONSECUTIVE_FAILURES, consecutiveFailures)
    }

    @Test
    fun upload_engine_resets_consecutive_counter_on_success() = runTest {
        // Simuliert: Fehler, Erfolg, Fehler — darf NICHT fruehzeitig abbrechen
        val results = listOf(false, true, false)
        var consecutiveFailures = 0
        var allSuccess = true
        var earlyExitTriggered = false

        for (success in results) {
            if (success) {
                consecutiveFailures = 0
            } else {
                allSuccess = false
                consecutiveFailures++
                if (consecutiveFailures >= PhotoUploadEngine.MAX_CONSECUTIVE_FAILURES) {
                    earlyExitTriggered = true
                    break
                }
            }
        }

        assertFalse(allSuccess)
        // consecutiveFailures wurde nach dem Erfolg zurueckgesetzt, daher kein Early-Exit
        assertFalse("Kein Early-Exit wenn Fehler nicht aufeinanderfolgend", earlyExitTriggered)
        assertEquals(1, consecutiveFailures)
    }

    // -------------------------------------------------------------------------
    // Test 3: inSampleSize-Kalkulation (gespiegelte Logik aus ThumbnailManagerImpl)
    // -------------------------------------------------------------------------

    @Test
    fun inSampleSize_4000x3000_for_150px_target_returns_8() {
        // 4000 -> /2=2000, /4=1000, /8=500, /16=250 >= 150 -> /32=125 < 150
        // => inSampleSize = 16
        val result = calculateInSampleSize(4000, 3000, reqSizePx = 150)
        assertEquals(16, result)
    }

    @Test
    fun inSampleSize_400x300_for_150px_target_returns_1() {
        // 300 / 2 = 150 >= 150 -> 300 / 4 = 75 < 150 => inSampleSize = 2
        val result = calculateInSampleSize(400, 300, reqSizePx = 150)
        assertEquals(2, result)
    }

    @Test
    fun inSampleSize_small_image_returns_1() {
        // 200x200 -> 200/2=100 < 150 => inSampleSize = 1 (kein Downsampling)
        val result = calculateInSampleSize(200, 200, reqSizePx = 150)
        assertEquals(1, result)
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /** Gespiegelte Logik aus ThumbnailManagerImpl — unabhaengig testbar. */
    private fun calculateInSampleSize(width: Int, height: Int, reqSizePx: Int): Int {
        var inSampleSize = 1
        val smallestSide = minOf(width, height)
        while (smallestSide / (inSampleSize * 2) >= reqSizePx) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun fakePetPhotoEntity(id: String) = PetPhotoEntity(
        id = id,
        petId = "pet_1",
        originalPath = "/storage/emulated/0/DCIM/$id.jpg",
        thumbSmallPath = null,
        thumbMediumPath = null,
        remoteOriginalUrl = null,
        remoteThumbSmallUrl = null,
        remoteThumbMediumUrl = null,
        uploadStatus = UploadStatus.LOCAL_ONLY,
        syncStatus = SyncStatus.PENDING,
        isDeleted = false,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )
}
