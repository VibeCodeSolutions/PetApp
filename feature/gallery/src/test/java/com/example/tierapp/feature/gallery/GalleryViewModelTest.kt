// feature/gallery/src/test/java/com/example/tierapp/feature/gallery/GalleryViewModelTest.kt
package com.example.tierapp.feature.gallery

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.tierapp.core.media.ThumbnailManager
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetPhotoRepository
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class GalleryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---- uiState -----------------------------------------------------------

    @Test
    fun `uiState ist Loading beim Start`() = runTest {
        val vm = buildViewModel()
        assertEquals(GalleryUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `uiState ist Empty wenn keine Fotos vorhanden`() = runTest {
        val vm = buildViewModel(photos = emptyList())
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(GalleryUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `uiState ist Success wenn Fotos vorhanden`() = runTest {
        val photo = buildTestPhoto()
        val vm = buildViewModel(photos = listOf(photo))
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is GalleryUiState.Success)
        assertEquals(1, (state as GalleryUiState.Success).photos.size)
    }

    // ---- importPhotos -------------------------------------------------------

    @Test
    fun `importPhotos fuegt Foto in Repository ein`() = runTest {
        val repo = FakePetPhotoRepository()
        val vm = buildViewModel(photoRepo = repo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.importPhotos(listOf(Uri.EMPTY))
        advanceUntilIdle()

        assertEquals(1, repo.insertedPhotos.size)
        assertEquals("test-pet", repo.insertedPhotos.first().petId)
        assertEquals("/fake/medium.jpg", repo.insertedPhotos.first().thumbMediumPath)
    }

    @Test
    fun `importPhotos ignoriert leere Liste`() = runTest {
        val repo = FakePetPhotoRepository()
        val vm = buildViewModel(photoRepo = repo)

        vm.importPhotos(emptyList())
        advanceUntilIdle()

        assertEquals(0, repo.insertedPhotos.size)
    }

    // ---- Vollbild -----------------------------------------------------------

    @Test
    fun `openFullscreen setzt fullscreenPhotoId`() = runTest {
        val vm = buildViewModel()
        assertNull(vm.fullscreenPhotoId.value)

        vm.openFullscreen("photo-1")
        assertEquals("photo-1", vm.fullscreenPhotoId.value)
    }

    @Test
    fun `closeFullscreen setzt fullscreenPhotoId auf null`() = runTest {
        val vm = buildViewModel()
        vm.openFullscreen("photo-1")
        vm.closeFullscreen()
        assertNull(vm.fullscreenPhotoId.value)
    }

    // ---- Löschen -----------------------------------------------------------

    @Test
    fun `confirmDelete loescht Foto und schliesst Dialog`() = runTest {
        val photo = buildTestPhoto(id = "photo-del")
        val repo = FakePetPhotoRepository(initialPhotos = listOf(photo))
        val vm = buildViewModel(photoRepo = repo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.requestDelete("photo-del")
        assertEquals("photo-del", vm.deleteDialogPhotoId.value)

        vm.confirmDelete()
        advanceUntilIdle()

        assertEquals(1, repo.deletedIds.size)
        assertEquals("photo-del", repo.deletedIds.first())
        assertNull(vm.deleteDialogPhotoId.value)
    }

    @Test
    fun `cancelDelete schliesst Dialog ohne Repository-Aufruf`() = runTest {
        val repo = FakePetPhotoRepository()
        val vm = buildViewModel(photoRepo = repo)

        vm.requestDelete("photo-x")
        vm.cancelDelete()

        assertNull(vm.deleteDialogPhotoId.value)
        assertEquals(0, repo.deletedIds.size)
    }

    @Test
    fun `confirmDelete schliesst auch Vollbild wenn geloeschtes Foto angezeigt wird`() = runTest {
        val photo = buildTestPhoto(id = "photo-fs")
        val repo = FakePetPhotoRepository(initialPhotos = listOf(photo))
        val vm = buildViewModel(photoRepo = repo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.openFullscreen("photo-fs")
        assertNotNull(vm.fullscreenPhotoId.value)

        vm.requestDelete("photo-fs")
        vm.confirmDelete()
        advanceUntilIdle()

        assertNull(vm.fullscreenPhotoId.value)
    }

    // ---- Hilfsfunktionen ---------------------------------------------------

    private fun buildViewModel(
        photos: List<PetPhoto> = emptyList(),
        photoRepo: FakePetPhotoRepository = FakePetPhotoRepository(photos),
    ): GalleryViewModel = GalleryViewModel(
        petPhotoRepository = photoRepo,
        thumbnailManager = FakeThumbnailManager,
        savedStateHandle = SavedStateHandle(mapOf("petId" to "test-pet")),
    )

    private fun buildTestPhoto(id: String = "photo-1") = PetPhoto(
        id = id,
        petId = "test-pet",
        originalPath = "/original.jpg",
        thumbSmallPath = "/small.jpg",
        thumbMediumPath = "/medium.jpg",
        uploadStatus = UploadStatus.LOCAL_ONLY,
        createdAt = Instant.EPOCH,
        syncStatus = SyncStatus.SYNCED,
        isDeleted = false,
    )

    // Shorthand für backgroundScope.launch(UnconfinedTestDispatcher)
    private fun kotlinx.coroutines.test.TestScope.collectInBackground(
        block: suspend () -> Unit,
    ) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { block() }
    }
}

// ---- Fakes -----------------------------------------------------------------

private class FakePetPhotoRepository(
    initialPhotos: List<PetPhoto> = emptyList(),
) : PetPhotoRepository {
    private val photosFlow = MutableStateFlow(initialPhotos)
    val insertedPhotos = mutableListOf<PetPhoto>()
    val deletedIds = mutableListOf<String>()

    override fun getByPetId(petId: String): Flow<List<PetPhoto>> =
        photosFlow.map { list -> list.filter { it.petId == petId && !it.isDeleted } }

    override suspend fun insert(photo: PetPhoto) {
        insertedPhotos += photo
        photosFlow.value = photosFlow.value + photo
    }

    override suspend fun delete(id: String) {
        deletedIds += id
        photosFlow.value = photosFlow.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
    }
}

private object FakeThumbnailManager : ThumbnailManager {
    override fun generateThumbs(sourceUri: Uri): ThumbnailManager.ThumbnailResult =
        ThumbnailManager.ThumbnailResult(
            thumbSmallPath = "/fake/small.jpg",
            thumbMediumPath = "/fake/medium.jpg",
        )
}
