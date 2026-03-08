package com.example.tierapp.feature.pets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

// ---- Route-Entry-Point (wird vom NavHost aufgerufen) --------------------

@Composable
fun PetListRoute(
    onAddPetClick: () -> Unit = {},
    onPetClick: (petId: String) -> Unit = {},
    viewModel: PetListViewModel = hiltViewModel(),
) {
    val uiState: PetListUiState by viewModel.uiState.collectAsStateWithLifecycle()
    PetListScreen(
        uiState = uiState,
        onAddPetClick = onAddPetClick,
        onPetClick = onPetClick,
        onRetry = viewModel::retry,
    )
}

// ---- Screen (erhält nur UiState + Lambdas) ------------------------------

@Composable
internal fun PetListScreen(
    uiState: PetListUiState,
    onAddPetClick: () -> Unit,
    onPetClick: (petId: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = "file:///android_asset/background1_Dashboard.png",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddPetClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tier hinzufügen",
                    )
                }
            },
        ) { innerPadding ->
        Crossfade(
            targetState = uiState,
            label = "pet_list_content",
            modifier = Modifier.padding(innerPadding),
        ) { state ->
            when (state) {
                PetListUiState.Loading -> LoadingContent()
                PetListUiState.Empty -> EmptyContent()
                is PetListUiState.Success -> PetList(
                    pets = state.pets,
                    onPetClick = onPetClick,
                )
                is PetListUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = onRetry,
                )
            }
        }
        }
    }
}

// ---- Inhaltsbereiche ----------------------------------------------------

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Noch keine Tiere vorhanden",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Tippe auf + um dein erstes Tier hinzuzufügen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text("Erneut versuchen")
            }
        }
    }
}

@Composable
private fun PetList(
    pets: List<PetSummary>,
    onPetClick: (petId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = pets,
            key = { pet -> pet.id },
        ) { pet ->
            PetCard(
                pet = pet,
                onClick = { onPetClick(pet.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

// ---- PetCard ------------------------------------------------------------

@Composable
private fun PetCard(
    pet: PetSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = pet.name }
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PetAvatar(thumbPath = pet.thumbSmallPath, petName = pet.name)
            Spacer(modifier = Modifier.width(12.dp))
            PetInfo(pet = pet)
        }
    }
}

@Composable
private fun PetAvatar(
    thumbPath: String?,
    petName: String,
    modifier: Modifier = Modifier,
) {
    val placeholderPainter = rememberVectorPainter(Icons.Default.Pets)
    AsyncImage(
        model = thumbPath,
        contentDescription = petName,
        placeholder = placeholderPainter,
        error = placeholderPainter,
        fallback = placeholderPainter,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun PetInfo(
    pet: PetSummary,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = pet.name,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = pet.species.toDisplayName(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        pet.breed?.let { breed ->
            Text(
                text = breed,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
