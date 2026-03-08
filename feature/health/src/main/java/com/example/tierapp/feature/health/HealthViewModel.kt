package com.example.tierapp.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.MedicationRepository
import com.example.tierapp.core.model.PetRepository
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.Vaccination
import com.example.tierapp.core.model.VaccinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _selectedPetId = MutableStateFlow<String?>(null)
    private val _dialogVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(
        petRepository.getAll(),
        _selectedPetId,
        _dialogVisible,
    ) { pets, selectedId, dialog ->
        Triple(pets, selectedId ?: pets.firstOrNull()?.id, dialog)
    }.flatMapLatest { (pets, petId, dialog) ->
        if (petId == null) {
            flowOf(HealthUiState(pets = pets, isLoading = false, showAddVaccinationDialog = dialog))
        } else {
            combine(
                vaccinationRepository.getByPetId(petId),
                vaccinationRepository.getUpcoming(daysAhead = 30),
                medicationRepository.getByPetId(petId),
            ) { vaccinations, upcoming, medications ->
                HealthUiState(
                    pets = pets,
                    selectedPetId = petId,
                    vaccinations = vaccinations.filter { !it.isDeleted },
                    upcomingVaccinations = upcoming.filter { !it.isDeleted },
                    medications = medications.filter { !it.isDeleted },
                    isLoading = false,
                    showAddVaccinationDialog = dialog,
                )
            }
        }
    }.catch { e ->
        emit(HealthUiState(isLoading = false, error = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HealthUiState(),
    )

    fun selectPet(petId: String) {
        _selectedPetId.value = petId
    }

    fun showAddVaccinationDialog() {
        _dialogVisible.value = true
    }

    fun dismissAddVaccinationDialog() {
        _dialogVisible.value = false
    }

    fun addVaccination(
        name: String,
        dateAdministered: LocalDate,
        intervalMonths: Int?,
        veterinarian: String?,
        notes: String?,
    ) {
        val petId = _selectedPetId.value ?: uiState.value.pets.firstOrNull()?.id ?: return
        viewModelScope.launch {
            val now = Instant.now()
            val nextDue = intervalMonths?.let { dateAdministered.plusMonths(it.toLong()) }
            vaccinationRepository.insert(
                Vaccination(
                    id = UUID.randomUUID().toString(),
                    petId = petId,
                    name = name,
                    dateAdministered = dateAdministered,
                    intervalMonths = intervalMonths,
                    veterinarian = veterinarian.takeIf { !it.isNullOrBlank() },
                    batchNumber = null,
                    notes = notes.takeIf { !it.isNullOrBlank() },
                    nextDueDate = nextDue,
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = SyncStatus.PENDING,
                    isDeleted = false,
                )
            )
            _dialogVisible.value = false
        }
    }

    fun dismissError() {
        // Error state is reset on next data emission; no explicit action needed
    }
}
