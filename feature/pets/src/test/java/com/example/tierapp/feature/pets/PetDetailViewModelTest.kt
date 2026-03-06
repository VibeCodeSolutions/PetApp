package com.example.tierapp.feature.pets

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.tierapp.core.media.ThumbnailManager
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetPhotoRepository
import com.example.tierapp.core.model.PetRepository
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class PetDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState ist Loading beim Start`() = runTest {
        val vm = buildViewModel()
        assertEquals(PetDetailUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `uiState ist Success wenn Pet existiert`() = runTest {
        val pet = buildTestPet()
        val vm = buildViewModel(pet = pet)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is PetDetailUiState.Success)
        assertEquals("Bello", (state as PetDetailUiState.Success).pet.name)
    }

    @Test
    fun `profilePhotoPath ist null wenn kein Profilbild vorhanden`() = runTest {
        val pet = buildTestPet(profilePhotoId = null)
        val vm = buildViewModel(pet = pet)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        val state = vm.uiState.value as PetDetailUiState.Success
        assertNull(state.profilePhotoPath)
    }

    @Test
    fun `uiState ist NotFound wenn Pet nicht existiert`() = runTest {
        val vm = buildViewModel(pet = null)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(PetDetailUiState.NotFound, vm.uiState.value)
    }

    @Test
    fun `onPhotoSelected speichert PetPhoto und aktualisiert Pet`() = runTest {
        val pet = buildTestPet()
        val petRepo = FakePetRepository(listOf(pet))
        val photoRepo = FakePetPhotoRepository()
        val vm = buildViewModel(pet = pet, petRepo = petRepo, photoRepo = photoRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        vm.onPhotoSelected(Uri.EMPTY)
        advanceUntilIdle()

        assertEquals(1, photoRepo.insertedPhotos.size)
        assertEquals(1, petRepo.updatedPets.size)
        assertEquals(photoRepo.insertedPhotos.first().id, petRepo.updatedPets.first().profilePhotoId)
    }

    // ---- Hilfsfunktionen ------------------------------------------------

    private fun buildViewModel(
        pet: Pet? = buildTestPet(),
        petRepo: FakePetRepository = FakePetRepository(listOfNotNull(pet)),
        photoRepo: FakePetPhotoRepository = FakePetPhotoRepository(),
    ): PetDetailViewModel = PetDetailViewModel(
        petRepository = petRepo,
        petPhotoRepository = photoRepo,
        thumbnailManager = FakeThumbnailManager,
        savedStateHandle = SavedStateHandle(mapOf("petId" to "test-1")),
    )

    private fun buildTestPet(profilePhotoId: String? = null): Pet = Pet(
        id = "test-1", name = "Bello", birthDate = null, species = PetSpecies.DOG, breed = null,
        chipNumber = null, color = null, weightKg = null, notes = null,
        profilePhotoId = profilePhotoId, familyId = null,
        createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
        syncStatus = SyncStatus.SYNCED, isDeleted = false,
    )
}

// ---- Fakes --------------------------------------------------------------

private class FakePetRepository(initialPets: List<Pet> = emptyList()) : PetRepository {
    private val petsFlow = MutableStateFlow(initialPets)
    val updatedPets = mutableListOf<Pet>()

    override fun getAll(): Flow<List<Pet>> = petsFlow
    override fun getById(id: String): Flow<Pet?> =
        petsFlow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(pet: Pet) { petsFlow.value = petsFlow.value + pet }
    override suspend fun update(pet: Pet) {
        updatedPets += pet
        petsFlow.value = petsFlow.value.map { if (it.id == pet.id) pet else it }
    }
    override suspend fun delete(id: String) {
        petsFlow.value = petsFlow.value.filter { it.id != id }
    }
}

private class FakePetPhotoRepository : PetPhotoRepository {
    private val photosFlow = MutableStateFlow<List<PetPhoto>>(emptyList())
    val insertedPhotos = mutableListOf<PetPhoto>()

    override fun getByPetId(petId: String): Flow<List<PetPhoto>> =
        photosFlow.map { list -> list.filter { it.petId == petId } }

    override suspend fun insert(photo: PetPhoto) {
        insertedPhotos += photo
        photosFlow.value = photosFlow.value + photo
    }

    override suspend fun delete(id: String) {
        photosFlow.value = photosFlow.value.filter { it.id != id }
    }
}

private object FakeThumbnailManager : ThumbnailManager {
    override fun generateThumbs(sourceUri: Uri): ThumbnailManager.ThumbnailResult =
        ThumbnailManager.ThumbnailResult(
            thumbSmallPath = "/fake/small.jpg",
            thumbMediumPath = "/fake/medium.jpg",
        )
}
