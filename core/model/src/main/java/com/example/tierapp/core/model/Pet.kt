package com.example.tierapp.core.model

import java.time.Instant
import java.time.LocalDate

data class Pet(
    val id: String,
    val name: String,
    val birthDate: LocalDate?,
    val species: PetSpecies,
    val breed: String?,
    val chipNumber: String?,
    val color: String?,
    val weightKg: Float?,
    val notes: String?,
    val profilePhotoId: String?,
    val familyId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)
