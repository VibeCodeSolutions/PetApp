package com.example.tierapp.feature.pets

import androidx.lifecycle.SavedStateHandle
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetRepository
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class PetEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---- Erstellen-Modus ------------------------------------------------

    @Test
    fun `Erstellen-Modus startet sofort mit Editing-State`() = runTest {
        val vm = buildCreateViewModel()
        assertTrue(vm.uiState.value is PetEditUiState.Editing)
    }

    @Test
    fun `Name-Fehler verschwindet beim naechsten onNameChange`() = runTest {
        val vm = buildCreateViewModel()
        vm.onSave()
        val withError = vm.uiState.value as PetEditUiState.Editing
        assertNotNull(withError.nameError)

        vm.onNameChange("Bello")
        assertNull((vm.uiState.value as PetEditUiState.Editing).nameError)
    }

    @Test
    fun `Chip-Nummer-Validierung schlaegt fehl bei falscher Laenge`() = runTest {
        val vm = buildCreateViewModel()
        vm.onNameChange("Bello")
        vm.onChipNumberChange("1234")
        vm.onSave()
        advanceUntilIdle()
        val state = vm.uiState.value as PetEditUiState.Editing
        assertNotNull(state.chipNumberError)
    }

    @Test
    fun `Chip-Nummer-Validierung besteht bei 15 Ziffern`() = runTest {
        val repo = FakePetRepository()
        val vm = buildCreateViewModel(repo)
        vm.onNameChange("Bello")
        vm.onChipNumberChange("123456789012345")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(PetEditUiState.SavedSuccess, vm.uiState.value)
    }

    @Test
    fun `Speichern ohne Name zeigt Name-Fehler`() = runTest {
        val vm = buildCreateViewModel()
        vm.onSave()
        val state = vm.uiState.value as PetEditUiState.Editing
        assertNotNull(state.nameError)
    }

    @Test
    fun `Erfolgreiches Speichern wechselt zu SavedSuccess`() = runTest {
        val repo = FakePetRepository()
        val vm = buildCreateViewModel(repo)
        vm.onNameChange("Mimi")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(PetEditUiState.SavedSuccess, vm.uiState.value)
        assertEquals(1, repo.insertedPets.size)
        assertEquals("Mimi", repo.insertedPets.first().name)
    }

    // ---- Bearbeiten-Modus -----------------------------------------------

    @Test
    fun `Bearbeiten-Modus laedt bestehende Pet-Daten`() = runTest {
        val existingPet = buildTestPet(name = "Rex")
        val repo = FakePetRepository(listOf(existingPet))
        val vm = buildEditViewModel(petId = existingPet.id, repo = repo)
        advanceUntilIdle()
        val state = vm.uiState.value as PetEditUiState.Editing
        assertEquals("Rex", state.name)
    }

    @Test
    fun `Bearbeiten-Modus ruft update statt insert auf`() = runTest {
        val existingPet = buildTestPet(name = "Rex")
        val repo = FakePetRepository(listOf(existingPet))
        val vm = buildEditViewModel(petId = existingPet.id, repo = repo)
        advanceUntilIdle()
        vm.onNameChange("Rex II")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(PetEditUiState.SavedSuccess, vm.uiState.value)
        assertTrue(repo.insertedPets.isEmpty())
        assertEquals("Rex II", repo.updatedPets.first().name)
    }

    // ---- Hilfsfunktionen ------------------------------------------------

    private fun buildCreateViewModel(repo: PetRepository = FakePetRepository()): PetEditViewModel =
        PetEditViewModel(repo, SavedStateHandle())

    private fun buildEditViewModel(petId: String, repo: PetRepository): PetEditViewModel =
        PetEditViewModel(repo, SavedStateHandle(mapOf("petId" to petId)))

    private fun buildTestPet(id: String = "test-1", name: String = "Bello"): Pet = Pet(
        id = id, name = name, birthDate = null, species = PetSpecies.DOG, breed = null,
        chipNumber = null, color = null, weightKg = null, notes = null, profilePhotoId = null,
        familyId = null, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
        syncStatus = SyncStatus.SYNCED, isDeleted = false,
    )
}

// ---- Fake-Implementierung -----------------------------------------------

private class FakePetRepository(initialPets: List<Pet> = emptyList()) : PetRepository {

    private val petsFlow = MutableStateFlow(initialPets)
    val insertedPets = mutableListOf<Pet>()
    val updatedPets = mutableListOf<Pet>()

    override fun getAll(): Flow<List<Pet>> = petsFlow
    override fun getById(id: String): Flow<Pet?> =
        petsFlow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(pet: Pet) {
        insertedPets += pet
        petsFlow.value = petsFlow.value + pet
    }

    override suspend fun update(pet: Pet) {
        updatedPets += pet
        petsFlow.value = petsFlow.value.map { if (it.id == pet.id) pet else it }
    }

    override suspend fun delete(id: String) {
        petsFlow.value = petsFlow.value.filter { it.id != id }
    }
}
