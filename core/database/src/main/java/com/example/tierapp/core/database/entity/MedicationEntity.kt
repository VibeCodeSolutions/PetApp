package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.Medication
import com.example.tierapp.core.model.SyncStatus
import java.time.Instant

@Entity(
    tableName = "medication",
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
data class MedicationEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val currentStock: Float,
    val dailyConsumption: Float,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)

fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    petId = petId,
    name = name,
    dosage = dosage,
    frequency = frequency,
    currentStock = currentStock,
    dailyConsumption = dailyConsumption,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    petId = petId,
    name = name,
    dosage = dosage,
    frequency = frequency,
    currentStock = currentStock,
    dailyConsumption = dailyConsumption,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
