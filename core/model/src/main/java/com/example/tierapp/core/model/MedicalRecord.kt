package com.example.tierapp.core.model

import java.time.Instant
import java.time.LocalDate

data class MedicalRecord(
    val id: String,
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
