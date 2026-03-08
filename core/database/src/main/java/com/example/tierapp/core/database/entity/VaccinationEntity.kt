package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.Vaccination
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "vaccination",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("petId")],
)
data class VaccinationEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val name: String,
    val dateAdministered: LocalDate,
    val intervalMonths: Int?,
    val veterinarian: String?,
    val batchNumber: String?,
    val notes: String?,
    val nextDueDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)

fun VaccinationEntity.toDomain(): Vaccination = Vaccination(
    id = id,
    petId = petId,
    name = name,
    dateAdministered = dateAdministered,
    intervalMonths = intervalMonths,
    veterinarian = veterinarian,
    batchNumber = batchNumber,
    notes = notes,
    nextDueDate = nextDueDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun Vaccination.toEntity(): VaccinationEntity = VaccinationEntity(
    id = id,
    petId = petId,
    name = name,
    dateAdministered = dateAdministered,
    intervalMonths = intervalMonths,
    veterinarian = veterinarian,
    batchNumber = batchNumber,
    notes = notes,
    nextDueDate = nextDueDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
