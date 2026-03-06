package com.example.tierapp.feature.pets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.ui.MediumDateFormatter
import java.io.File

private const val FILE_PROVIDER_AUTHORITY = "com.example.tierapp.fileprovider"

// ---- Route-Entry-Point --------------------------------------------------

@Composable
fun PetDetailRoute(
    onEditClick: (petId: String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { error ->
            when (error) {
                PetDetailViewModel.PhotoError.GenerateFailed ->
                    snackbarHostState.showSnackbar("Foto konnte nicht verarbeitet werden")
            }
        }
    }

    PetDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEditClick = onEditClick,
        onBackClick = onBackClick,
        onPhotoSelected = viewModel::onPhotoSelected,
    )
}

// ---- Screen -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PetDetailScreen(
    uiState: PetDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEditClick: (petId: String) -> Unit,
    onBackClick: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val name = (uiState as? PetDetailUiState.Success)?.pet?.name ?: ""
                    Text(name)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState is PetDetailUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = { onEditClick(uiState.pet.id) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Bearbeiten") },
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            PetDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            PetDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { Text("Tier nicht gefunden") }
            }
            is PetDetailUiState.Success -> {
                PetDetailContent(
                    pet = uiState.pet,
                    profilePhotoPath = uiState.profilePhotoPath,
                    onPhotoSelected = onPhotoSelected,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

// ---- Detail-Inhalt ------------------------------------------------------

@Composable
private fun PetDetailContent(
    pet: Pet,
    profilePhotoPath: String?,
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showPhotoDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { onPhotoSelected(it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { onPhotoSelected(it) }
        } else {
            // Temp-Datei aufräumen, wenn Kamera abgebrochen wurde
            cameraImageUri?.path?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            cameraImageUri = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfilePhoto(
            photoPath = profilePhotoPath,
            petName = pet.name,
            onChangePhoto = { showPhotoDialog = true },
        )

        Spacer(modifier = Modifier.height(24.dp))

        PetInfoSection(pet = pet)
    }

    if (showPhotoDialog) {
        PhotoSourceDialog(
            onGalleryClick = {
                showPhotoDialog = false
                galleryLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onCameraClick = {
                showPhotoDialog = false
                val tempFile = File(
                    context.cacheDir,
                    "camera_${System.currentTimeMillis()}.jpg",
                )
                val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, tempFile)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            },
            onDismiss = { showPhotoDialog = false },
        )
    }
}

@Composable
private fun ProfilePhoto(
    photoPath: String?,
    petName: String,
    onChangePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholderPainter = rememberVectorPainter(Icons.Default.Pets)
    Box(modifier = modifier) {
        AsyncImage(
            model = photoPath,
            contentDescription = petName,
            placeholder = placeholderPainter,
            error = placeholderPainter,
            fallback = placeholderPainter,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        IconButton(
            onClick = onChangePhoto,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Foto ändern",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PetInfoSection(
    pet: Pet,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        InfoRow(label = "Tierart", value = pet.species.toDisplayName())
        pet.breed?.let { InfoRow(label = "Rasse", value = it) }
        pet.birthDate?.let { InfoRow(label = "Geburtsdatum", value = it.format(MediumDateFormatter)) }
        pet.chipNumber?.let { InfoRow(label = "Chip-Nummer", value = it) }
        pet.color?.let { InfoRow(label = "Farbe", value = it) }
        pet.weightKg?.let { InfoRow(label = "Gewicht", value = "${"%.2f".format(it)} kg") }
        pet.notes?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Notizen",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// ---- Foto-Quelle Dialog -------------------------------------------------

@Composable
private fun PhotoSourceDialog(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto auswählen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null)
                    Text("  Aus Galerie wählen")
                }
                TextButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text("  Kamera")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
