// feature/gallery/src/main/java/com/example/tierapp/feature/gallery/GalleryScreen.kt
package com.example.tierapp.feature.gallery

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

// ---- Route-Entry-Point -----------------------------------------------------

@Composable
fun GalleryRoute(
    onBackClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fullscreenPhotoId by viewModel.fullscreenPhotoId.collectAsStateWithLifecycle()
    val deleteDialogPhotoId by viewModel.deleteDialogPhotoId.collectAsStateWithLifecycle()

    val multiPickerLauncher = rememberLauncherForActivityResult(PickMultipleVisualMedia()) { uris ->
        viewModel.importPhotos(uris)
    }

    GalleryScreen(
        uiState = uiState,
        fullscreenPhotoId = fullscreenPhotoId,
        deleteDialogPhotoId = deleteDialogPhotoId,
        onBackClick = onBackClick,
        onAddClick = {
            multiPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        },
        onPhotoClick = viewModel::openFullscreen,
        onCloseFullscreen = viewModel::closeFullscreen,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
    )
}

// ---- Screen ----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryScreen(
    uiState: GalleryUiState,
    fullscreenPhotoId: String?,
    deleteDialogPhotoId: String?,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onPhotoClick: (photoId: String) -> Unit,
    onCloseFullscreen: () -> Unit,
    onRequestDelete: (photoId: String) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Photos aus dem aktuellen Success-State (für Vollbild-Lookup)
    val photos = (uiState as? GalleryUiState.Success)?.photos.orEmpty()
    val fullscreenPhoto = photos.firstOrNull { it.id == fullscreenPhotoId }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Foto-Galerie") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Fotos hinzufügen")
                }
            },
        ) { innerPadding ->
            when (uiState) {
                GalleryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
                GalleryUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Noch keine Fotos. Tippe auf + um welche hinzuzufügen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is GalleryUiState.Success -> {
                    PhotoGrid(
                        photos = uiState.photos,
                        onPhotoClick = { photo -> onPhotoClick(photo.id) },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

        // Vollbild-Overlay — liegt über dem Scaffold, kein NavigationBar-Padding
        if (fullscreenPhoto != null) {
            FullscreenPhotoView(
                photoPath = fullscreenPhoto.originalPath,
                onClose = onCloseFullscreen,
                onDelete = { onRequestDelete(fullscreenPhoto.id) },
            )
        }
    }

    // Löschen-Bestätigungs-Dialog
    if (deleteDialogPhotoId != null) {
        DeleteConfirmDialog(
            onConfirm = onConfirmDelete,
            onDismiss = onCancelDelete,
        )
    }
}

// ---- Foto-Grid -------------------------------------------------------------

@Composable
private fun PhotoGrid(
    photos: List<com.example.tierapp.core.model.PetPhoto>,
    onPhotoClick: (com.example.tierapp.core.model.PetPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholderPainter = rememberVectorPainter(Icons.Default.Photo)
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id }) { _, photo ->
            // Thumb-M (400×400) im Grid — niemals Originale
            AsyncImage(
                model = photo.thumbMediumPath,
                contentDescription = null,
                placeholder = placeholderPainter,
                error = placeholderPainter,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onPhotoClick(photo) },
            )
        }
    }
}

// ---- Vollbild-Ansicht mit Zoom ---------------------------------------------

@Composable
private fun FullscreenPhotoView(
    photoPath: String,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    BackHandler(onBack = onClose)

    // scale per remember — lebt nur in Composition, kein ViewModel-State → kein Memory Leak
    // key = photoPath: Scale wird zurückgesetzt wenn ein anderes Foto geöffnet wird
    var scale by remember(photoPath) { mutableFloatStateOf(1f) }
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AsyncImage(
            model = photoPath,
            contentDescription = "Vollbild",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .transformable(state = transformableState),
        )

        // Top-Leiste mit Schließen & Löschen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Schließen",
                    tint = Color.White,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Foto löschen",
                    tint = Color.White,
                )
            }
        }
    }
}

// ---- Löschen-Dialog --------------------------------------------------------

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto löschen?") },
        text = { Text("Das Foto wird unwiderruflich gelöscht.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Löschen") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
