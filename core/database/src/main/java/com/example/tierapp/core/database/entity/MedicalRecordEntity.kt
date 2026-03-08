package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.MedicalRecord
import com.example.tierapp.core.model.MedicalRecordType
import com.example.tierapp.core.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "medical_record",
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
data class MedicalRecordEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val type: MedicalRecordType,
    val title: String,
    val description: String?,
    val date: LocalDate,
    val veterinarian: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)

fun MedicalRecordEntity.toDomain(): MedicalRecord = MedicalRecord(
    id = id,
    petId = petId,
    type = type,
    title = title,
    description = description,
    date = date,
    veterinarian = veterinarian,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun MedicalRecord.toEntity(): MedicalRecordEntity = MedicalRecordEntity(
    id = id,
    petId = petId,
    type = type,
    title = title,
    description = description,
    date = date,
    veterinarian = veterinarian,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
