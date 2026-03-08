package com.example.tierapp.core.model

import java.time.Instant

data class Reminder(
    val id: String,
    val petId: String,
    val type: ReminderType,
    val title: String,
    val referenceId: String,
    val triggerAt: Instant,
    val isCompleted: Boolean,
    val isSnoozed: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)
