package com.example.tierapp.feature.pets

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.media.ThumbnailManager
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetPhotoRepository
import com.example.tierapp.core.model.PetRepository
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val petPhotoRepository: PetPhotoRepository,
    private val thumbnailManager: ThumbnailManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    val uiState: StateFlow<PetDetailUiState> = petRepository.getById(petId)
        .combine(petPhotoRepository.getByPetId(petId)) { pet, photos ->
            if (pet == null) return@combine PetDetailUiState.NotFound
            val profilePhoto = photos.firstOrNull { it.id == pet.profilePhotoId }
            PetDetailUiState.Success(
                pet = pet,
                profilePhotoPath = profilePhoto?.thumbMediumPath,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PetDetailUiState.Loading,
        )

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            val thumbs = thumbnailManager.generateThumbs(uri)
            val photoId = UUID.randomUUID().toString()

            petPhotoRepository.insert(
                PetPhoto(
                    id = photoId,
                    petId = petId,
                    originalPath = uri.toString(),
                    thumbSmallPath = thumbs.thumbSmallPath,
                    thumbMediumPath = thumbs.thumbMediumPath,
                    uploadStatus = UploadStatus.LOCAL_ONLY,
                    createdAt = Instant.now(),
                    syncStatus = SyncStatus.PENDING,
                    isDeleted = false,
                )
            )

            val current = (uiState.value as? PetDetailUiState.Success)?.pet ?: return@launch
            petRepository.update(
                current.copy(
                    profilePhotoId = photoId,
                    updatedAt = Instant.now(),
                    syncStatus = SyncStatus.PENDING,
                )
            )
        }
    }
}
