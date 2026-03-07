// feature/gallery/src/main/java/com/example/tierapp/feature/gallery/GalleryViewModel.kt
package com.example.tierapp.feature.gallery

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.media.ThumbnailManager
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetPhotoRepository
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val petPhotoRepository: PetPhotoRepository,
    private val thumbnailManager: ThumbnailManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    // ---- Foto-Liste --------------------------------------------------------

    val uiState: StateFlow<GalleryUiState> = petPhotoRepository.getByPetId(petId)
        .map { photos ->
            if (photos.isEmpty()) GalleryUiState.Empty else GalleryUiState.Success(photos)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GalleryUiState.Loading,
        )

    // ---- Vollbild ----------------------------------------------------------
    // Speichert Photo-ID statt Index → bleibt gültig wenn Liste sich ändert

    private val _fullscreenPhotoId = MutableStateFlow<String?>(null)
    val fullscreenPhotoId: StateFlow<String?> = _fullscreenPhotoId.asStateFlow()

    fun openFullscreen(photoId: String) { _fullscreenPhotoId.value = photoId }
    fun closeFullscreen() { _fullscreenPhotoId.value = null }

    // ---- Löschen-Dialog ----------------------------------------------------

    private val _deleteDialogPhotoId = MutableStateFlow<String?>(null)
    val deleteDialogPhotoId: StateFlow<String?> = _deleteDialogPhotoId.asStateFlow()

    fun requestDelete(photoId: String) { _deleteDialogPhotoId.value = photoId }
    fun cancelDelete() { _deleteDialogPhotoId.value = null }

    fun confirmDelete() {
        val id = _deleteDialogPhotoId.value ?: return
        _deleteDialogPhotoId.value = null
        // Vollbild schließen wenn das gelöschte Foto gerade angezeigt wird
        if (_fullscreenPhotoId.value == id) _fullscreenPhotoId.value = null
        viewModelScope.launch { petPhotoRepository.delete(id) }
    }

    // ---- Multi-Import ------------------------------------------------------

    fun importPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            uris.forEach { uri ->
                runCatching {
                    // generateThumbs() ist blocking (BitmapFactory + File-I/O) → IO-Dispatcher
                    val thumbs = withContext(Dispatchers.IO) { thumbnailManager.generateThumbs(uri) }
                    petPhotoRepository.insert(
                        PetPhoto(
                            id = UUID.randomUUID().toString(),
                            petId = petId,
                            originalPath = uri.toString(),
                            thumbSmallPath = thumbs.thumbSmallPath,
                            thumbMediumPath = thumbs.thumbMediumPath,
                            uploadStatus = UploadStatus.LOCAL_ONLY,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now(),
                            syncStatus = SyncStatus.PENDING,
                            isDeleted = false,
                        )
                    )
                }
                // Einzelne Fehler (z.B. korrupte Datei) überspringen, Import läuft weiter
            }
        }
    }
}
