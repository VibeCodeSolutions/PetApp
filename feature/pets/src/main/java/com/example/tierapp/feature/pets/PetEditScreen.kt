package com.example.tierapp.feature.pets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tierapp.core.model.PetSpecies
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

// ---- Route-Entry-Point --------------------------------------------------

@Composable
fun PetEditRoute(
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: PetEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is PetEditUiState.SavedSuccess) onSaved()
    }

    PetEditScreen(
        uiState = uiState,
        isEditMode = viewModel.isEditMode,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onSpeciesChange = viewModel::onSpeciesChange,
        onBreedChange = viewModel::onBreedChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onChipNumberChange = viewModel::onChipNumberChange,
        onColorChange = viewModel::onColorChange,
        onWeightKgChange = viewModel::onWeightKgChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::onSave,
    )
}

// ---- Screen -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PetEditScreen(
    uiState: PetEditUiState,
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (PetSpecies) -> Unit,
    onBreedChange: (String) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onChipNumberChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onWeightKgChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Tier bearbeiten" else "Tier hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is PetEditUiState.Loading, is PetEditUiState.SavedSuccess -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            is PetEditUiState.Editing -> {
                PetEditForm(
                    state = uiState,
                    onNameChange = onNameChange,
                    onSpeciesChange = onSpeciesChange,
                    onBreedChange = onBreedChange,
                    onBirthDateChange = onBirthDateChange,
                    onChipNumberChange = onChipNumberChange,
                    onColorChange = onColorChange,
                    onWeightKgChange = onWeightKgChange,
                    onNotesChange = onNotesChange,
                    onSave = onSave,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

// ---- Formular -----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetEditForm(
    state: PetEditUiState.Editing,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (PetSpecies) -> Unit,
    onBreedChange: (String) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onChipNumberChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onWeightKgChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Name (Pflichtfeld)
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Name *") },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Tierart (Dropdown)
        SpeciesDropdown(
            selected = state.species,
            onSpeciesChange = onSpeciesChange,
            modifier = Modifier.fillMaxWidth(),
        )

        // Rasse
        OutlinedTextField(
            value = state.breed,
            onValueChange = onBreedChange,
            label = { Text("Rasse") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Geburtsdatum
        OutlinedTextField(
            value = state.birthDate?.format(DATE_FORMATTER) ?: "",
            onValueChange = {},
            label = { Text("Geburtsdatum") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Datum auswählen")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Chip-Nummer
        OutlinedTextField(
            value = state.chipNumber,
            onValueChange = onChipNumberChange,
            label = { Text("Chip-Nummer (15 Ziffern)") },
            isError = state.chipNumberError != null,
            supportingText = state.chipNumberError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Farbe
        OutlinedTextField(
            value = state.color,
            onValueChange = onColorChange,
            label = { Text("Farbe / Fellfarbe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Gewicht
        OutlinedTextField(
            value = state.weightKg,
            onValueChange = onWeightKgChange,
            label = { Text("Gewicht (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Notizen
        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = { Text("Notizen") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("Speichern")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDatePicker) {
        BirthDatePickerDialog(
            initialDate = state.birthDate,
            onDateSelected = { date ->
                onBirthDateChange(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

// ---- Species Dropdown ---------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeciesDropdown(
    selected: PetSpecies,
    onSpeciesChange: (PetSpecies) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.toDisplayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tierart *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PetSpecies.entries.forEach { species ->
                DropdownMenuItem(
                    text = { Text(species.toDisplayName()) },
                    onClick = {
                        onSpeciesChange(species)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ---- DatePicker Dialog --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = initialDate
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                val date = selectedMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                }
                onDateSelected(date)
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

// ---- Erweiterungsfunktion -----------------------------------------------

private fun PetSpecies.toDisplayName(): String = when (this) {
    PetSpecies.DOG -> "Hund"
    PetSpecies.CAT -> "Katze"
    PetSpecies.BIRD -> "Vogel"
    PetSpecies.RABBIT -> "Kaninchen"
    PetSpecies.GUINEA_PIG -> "Meerschweinchen"
    PetSpecies.HAMSTER -> "Hamster"
    PetSpecies.FISH -> "Fisch"
    PetSpecies.REPTILE -> "Reptil"
    PetSpecies.OTHER -> "Sonstiges"
}
