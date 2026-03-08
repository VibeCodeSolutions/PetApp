package com.example.tierapp.core.model

import java.time.Instant

data class Medication(
    val id: String,
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
) {
    val daysRemaining: Float
        get() = if (dailyConsumption > 0f) currentStock / dailyConsumption else Float.MAX_VALUE

    val isLowStock: Boolean
        get() = daysRemaining <= 7f
}
