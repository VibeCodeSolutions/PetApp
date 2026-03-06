package com.example.tierapp.feature.pets

import com.example.tierapp.core.model.PetSpecies
import java.time.LocalDate

sealed interface PetEditUiState {
    data object Loading : PetEditUiState

    data class Editing(
        val name: String = "",
        val nameError: String? = null,
        val species: PetSpecies = PetSpecies.DOG,
        val breed: String = "",
        val birthDate: LocalDate? = null,
        val chipNumber: String = "",
        val chipNumberError: String? = null,
        val color: String = "",
        val weightKg: String = "",
        val notes: String = "",
        val isSaving: Boolean = false,
    ) : PetEditUiState

    data object SavedSuccess : PetEditUiState
}
