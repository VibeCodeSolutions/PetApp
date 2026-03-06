package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey val id: String,
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

fun PetEntity.toDomain(): Pet = Pet(
    id = id,
    name = name,
    birthDate = birthDate,
    species = species,
    breed = breed,
    chipNumber = chipNumber,
    color = color,
    weightKg = weightKg,
    notes = notes,
    profilePhotoId = profilePhotoId,
    familyId = familyId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun Pet.toEntity(): PetEntity = PetEntity(
    id = id,
    name = name,
    birthDate = birthDate,
    species = species,
    breed = breed,
    chipNumber = chipNumber,
    color = color,
    weightKg = weightKg,
    notes = notes,
    profilePhotoId = profilePhotoId,
    familyId = familyId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
