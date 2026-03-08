package com.example.tierapp.core.model

import java.time.Instant
import java.time.LocalDate

data class Vaccination(
    val id: String,
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
