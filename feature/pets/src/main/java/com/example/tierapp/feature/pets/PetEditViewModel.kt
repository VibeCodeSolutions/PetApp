package com.example.tierapp.feature.pets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetRepository
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

private val CHIP_NUMBER_REGEX = Regex("^\\d{15}$")

@HiltViewModel
class PetEditViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val petId: String? = savedStateHandle["petId"]
    val isEditMode: Boolean = petId != null

    private val _uiState = MutableStateFlow<PetEditUiState>(
        if (petId == null) PetEditUiState.Editing() else PetEditUiState.Loading
    )
    val uiState: StateFlow<PetEditUiState> = _uiState.asStateFlow()

    init {
        if (petId != null) {
            viewModelScope.launch {
                val pet = petRepository.getById(petId).first()
                _uiState.value = if (pet != null) {
                    PetEditUiState.Editing(
                        name = pet.name,
                        species = pet.species,
                        breed = pet.breed.orEmpty(),
                        birthDate = pet.birthDate,
                        chipNumber = pet.chipNumber.orEmpty(),
                        color = pet.color.orEmpty(),
                        weightKg = pet.weightKg?.toString().orEmpty(),
                        notes = pet.notes.orEmpty(),
                    )
                } else {
                    PetEditUiState.Editing()
                }
            }
        }
    }

    fun onNameChange(value: String) = updateEditing { copy(name = value, nameError = null) }
    fun onSpeciesChange(value: PetSpecies) = updateEditing { copy(species = value) }
    fun onBreedChange(value: String) = updateEditing { copy(breed = value) }
    fun onBirthDateChange(value: LocalDate?) = updateEditing { copy(birthDate = value) }
    fun onChipNumberChange(value: String) = updateEditing { copy(chipNumber = value, chipNumberError = null) }
    fun onColorChange(value: String) = updateEditing { copy(color = value) }
    fun onWeightKgChange(value: String) = updateEditing { copy(weightKg = value) }
    fun onNotesChange(value: String) = updateEditing { copy(notes = value) }

    fun onSave() {
        val current = _uiState.value as? PetEditUiState.Editing ?: return
        if (!validate(current)) return

        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            if (isEditMode) {
                val existing = petRepository.getById(checkNotNull(petId)).first() ?: return@launch
                petRepository.update(existing.toUpdated(current))
            } else {
                petRepository.insert(current.toPet())
            }
            _uiState.value = PetEditUiState.SavedSuccess
        }
    }

    private fun validate(state: PetEditUiState.Editing): Boolean {
        var valid = true
        var updated = state

        if (state.name.isBlank()) {
            updated = updated.copy(nameError = "Name ist ein Pflichtfeld")
            valid = false
        }
        if (state.chipNumber.isNotBlank() && !CHIP_NUMBER_REGEX.matches(state.chipNumber)) {
            updated = updated.copy(chipNumberError = "Chip-Nummer muss genau 15 Ziffern haben")
            valid = false
        }

        if (!valid) _uiState.value = updated
        return valid
    }

    private fun updateEditing(transform: PetEditUiState.Editing.() -> PetEditUiState.Editing) {
        val current = _uiState.value as? PetEditUiState.Editing ?: return
        _uiState.value = current.transform()
    }

    private fun PetEditUiState.Editing.toPet(): Pet = Pet(
        id = UUID.randomUUID().toString(),
        name = name.trim(),
        species = species,
        breed = breed.trimToNullIfBlank(),
        birthDate = birthDate,
        chipNumber = chipNumber.trimToNullIfBlank(),
        color = color.trimToNullIfBlank(),
        weightKg = weightKg.toFloatOrNull(),
        notes = notes.trimToNullIfBlank(),
        profilePhotoId = null,
        familyId = null,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        syncStatus = SyncStatus.PENDING,
        isDeleted = false,
    )

    private fun Pet.toUpdated(state: PetEditUiState.Editing): Pet = copy(
        name = state.name.trim(),
        species = state.species,
        breed = state.breed.trimToNullIfBlank(),
        birthDate = state.birthDate,
        chipNumber = state.chipNumber.trimToNullIfBlank(),
        color = state.color.trimToNullIfBlank(),
        weightKg = state.weightKg.toFloatOrNull(),
        notes = state.notes.trimToNullIfBlank(),
        updatedAt = Instant.now(),
        syncStatus = SyncStatus.PENDING,
    )

    private fun String.trimToNullIfBlank(): String? = trim().ifBlank { null }
}
