package com.example.tierapp.feature.pets

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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class PetListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---- Hilfsfunktionen ------------------------------------------------

    private fun buildTestPet(
        id: String = "test-1",
        name: String = "Bello",
    ): Pet = Pet(
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
        familyId = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        syncStatus = SyncStatus.SYNCED,
        isDeleted = false,
    )

    private fun buildViewModel(initialPets: List<Pet> = emptyList()): PetListViewModel =
        PetListViewModel(FakePetRepository(initialPets), FakePetPhotoRepository())

    // ---- Tests ----------------------------------------------------------

    @Test
    fun `uiState ist Loading beim Start vor dem ersten Collector`() = runTest {
        val vm = buildViewModel()
        assertEquals(PetListUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `uiState ist Empty wenn Repository leer ist`() = runTest {
        val vm = buildViewModel(initialPets = emptyList())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(PetListUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `uiState ist Success wenn mindestens ein Tier vorhanden ist`() = runTest {
        val pet = buildTestPet()
        val vm = buildViewModel(initialPets = listOf(pet))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue("Erwartet Success, war: $state", state is PetListUiState.Success)
        assertEquals(1, (state as PetListUiState.Success).pets.size)
        assertEquals("Bello", state.pets.first().name)
    }

    @Test
    fun `uiState wechselt von Empty zu Success wenn ein Tier hinzugefuegt wird`() = runTest {
        val fakeRepo = FakePetRepository(emptyList())
        val vm = PetListViewModel(fakeRepo, FakePetPhotoRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(PetListUiState.Empty, vm.uiState.value)

        fakeRepo.emit(listOf(buildTestPet()))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PetListUiState.Success)
    }
}

// ---- Fake-Implementierungen ---------------------------------------------

private class FakePetRepository(initialPets: List<Pet> = emptyList()) : PetRepository {

    private val petsFlow = MutableStateFlow(initialPets)

    fun emit(pets: List<Pet>) {
        petsFlow.value = pets
    }

    override fun getAll(): Flow<List<Pet>> = petsFlow

    override fun getById(id: String): Flow<Pet?> =
        petsFlow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(pet: Pet) {
        petsFlow.value = petsFlow.value + pet
    }

    override suspend fun update(pet: Pet) {
        petsFlow.value = petsFlow.value.map { if (it.id == pet.id) pet else it }
    }

    override suspend fun delete(id: String) {
        petsFlow.value = petsFlow.value.filter { it.id != id }
    }
}

private class FakePetPhotoRepository : PetPhotoRepository {
    private val photosFlow = MutableStateFlow<List<PetPhoto>>(emptyList())

    override fun getByPetId(petId: String): Flow<List<PetPhoto>> =
        photosFlow.map { list -> list.filter { it.petId == petId } }

    override suspend fun insert(photo: PetPhoto) {
        photosFlow.value = photosFlow.value + photo
    }

    override suspend fun delete(id: String) {
        photosFlow.value = photosFlow.value.filter { it.id != id }
    }
}
