package com.example.tierapp.feature.pets

import com.example.tierapp.core.model.Pet

sealed interface PetListUiState {
    data object Loading : PetListUiState
    data class Success(val pets: List<Pet>) : PetListUiState
    data object Empty : PetListUiState
}
