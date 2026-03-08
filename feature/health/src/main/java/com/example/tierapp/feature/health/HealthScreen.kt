package com.example.tierapp.feature.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tierapp.core.model.Medication
import com.example.tierapp.core.model.Vaccination
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun HealthRoute(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HealthScreen(
        uiState = uiState,
        onSelectPet = viewModel::selectPet,
        onAddVaccinationClick = viewModel::showAddVaccinationDialog,
        onAddVaccination = viewModel::addVaccination,
        onDismissDialog = viewModel::dismissAddVaccinationDialog,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HealthScreen(
    uiState: HealthUiState,
    onSelectPet: (String) -> Unit,
    onAddVaccinationClick: () -> Unit,
    onAddVaccination: (String, LocalDate, Int?, String?, String?) -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Gesundheit") })
        },
        floatingActionButton = {
            if (uiState.selectedPetId != null) {
                FloatingActionButton(onClick = onAddVaccinationClick) {
                    Icon(Icons.Default.Add, contentDescription = "Impfung hinzufügen")
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.pets.isEmpty() -> {
                EmptyPetsHint(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding))
            }

            else -> {
                HealthContent(
                    uiState = uiState,
                    onSelectPet = onSelectPet,
                    contentPadding = innerPadding,
                )
            }
        }
    }

    if (uiState.showAddVaccinationDialog) {
        AddVaccinationDialog(
            onConfirm = onAddVaccination,
            onDismiss = onDismissDialog,
        )
    }
}

@Composable
private fun EmptyPetsHint(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Noch keine Tiere vorhanden",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Füge zuerst ein Tier im Tiere-Tab hinzu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HealthContent(
    uiState: HealthUiState,
    onSelectPet: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Pet selector
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.pets) { pet ->
                    FilterChip(
                        selected = pet.id == uiState.selectedPetId,
                        onClick = { onSelectPet(pet.id) },
                        label = { Text(pet.name) },
                    )
                }
            }
        }

        // Upcoming vaccinations banner
        if (uiState.upcomingVaccinations.isNotEmpty()) {
            item {
                UpcomingBanner(
                    count = uiState.upcomingVaccinations.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // Vaccinations section
        item {
            SectionHeader(
                title = "Impfungen",
                icon = Icons.Default.Vaccines,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (uiState.vaccinations.isEmpty()) {
            item {
                Text(
                    text = "Noch keine Impfungen eingetragen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        } else {
            items(uiState.vaccinations, key = { it.id }) { vaccination ->
                VaccinationCard(
                    vaccination = vaccination,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // Medications section
        item {
            SectionHeader(
                title = "Medikamente",
                icon = Icons.Default.Medication,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (uiState.medications.isEmpty()) {
            item {
                Text(
                    text = "Keine aktiven Medikamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        } else {
            items(uiState.medications, key = { it.id }) { medication ->
                MedicationCard(
                    medication = medication,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        item { Spacer(Modifier.height(88.dp)) } // FAB clearance
    }
}

@Composable
private fun UpcomingBanner(count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$count fällige Impfung${if (count > 1) "en" else ""} in den nächsten 30 Tagen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun VaccinationCard(vaccination: Vaccination, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = vaccination.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Verabreicht: ${vaccination.dateAdministered.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            vaccination.nextDueDate?.let { due ->
                val isOverdue = due.isBefore(LocalDate.now())
                Text(
                    text = "Nächste Fälligkeit: ${due.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
            vaccination.veterinarian?.let {
                Text(
                    text = "Tierarzt: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MedicationCard(medication: Medication, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isLowStock)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (medication.isLowStock) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Geringer Vorrat",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = "Dosis: ${medication.dosage} — ${medication.frequency}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (medication.dailyConsumption > 0f) {
                val days = medication.daysRemaining.toInt()
                Text(
                    text = "Vorrat noch ca. $days Tage",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (medication.isLowStock) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddVaccinationDialog(
    onConfirm: (String, LocalDate, Int?, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var intervalText by rememberSaveable { mutableStateOf("") }
    var veterinarian by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Impfung eintragen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Impfstoff *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Datum (JJJJ-MM-TT) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it },
                    label = { Text("Wiederholungsintervall (Monate)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = veterinarian,
                    onValueChange = { veterinarian = it },
                    label = { Text("Tierarzt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true; return@TextButton }
                val date = runCatching { LocalDate.parse(dateText) }.getOrNull()
                    ?: LocalDate.now()
                val interval = intervalText.trim().toIntOrNull()
                onConfirm(name.trim(), date, interval, veterinarian, notes)
            }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
