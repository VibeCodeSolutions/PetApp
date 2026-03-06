package com.example.tierapp.feature.pets

import com.example.tierapp.core.model.Pet

sealed interface PetDetailUiState {
    data object Loading : PetDetailUiState
    data class Success(
        val pet: Pet,
        val profilePhotoPath: String?,
    ) : PetDetailUiState
    data object NotFound : PetDetailUiState
}
