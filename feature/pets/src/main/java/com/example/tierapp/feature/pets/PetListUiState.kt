package com.example.tierapp.feature.pets

import com.example.tierapp.core.model.PetSpecies

data class PetSummary(
    val id: String,
    val name: String,
    val species: PetSpecies,
    val breed: String?,
    val thumbSmallPath: String?,
)

sealed interface PetListUiState {
    data object Loading : PetListUiState
    data object Empty : PetListUiState
    data class Success(val pets: List<PetSummary>) : PetListUiState
}
