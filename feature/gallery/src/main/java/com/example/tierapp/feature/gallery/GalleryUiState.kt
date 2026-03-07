// feature/gallery/src/main/java/com/example/tierapp/feature/gallery/GalleryUiState.kt
package com.example.tierapp.feature.gallery

import com.example.tierapp.core.model.PetPhoto

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data object Empty : GalleryUiState
    data class Success(val photos: List<PetPhoto>) : GalleryUiState
}
