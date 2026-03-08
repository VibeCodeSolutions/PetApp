package com.example.tierapp.feature.health

import com.example.tierapp.core.model.Medication
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.Vaccination

data class HealthUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPetId: String? = null,
    val vaccinations: List<Vaccination> = emptyList(),
    val upcomingVaccinations: List<Vaccination> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddVaccinationDialog: Boolean = false,
) {
    val selectedPet: Pet? get() = pets.find { it.id == selectedPetId }
}
