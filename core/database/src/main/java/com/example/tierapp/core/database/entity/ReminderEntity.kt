package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.Reminder
import com.example.tierapp.core.model.ReminderType
import com.example.tierapp.core.model.SyncStatus
import java.time.Instant

@Entity(
    tableName = "reminder",
    indices = [
        Index(value = ["referenceId", "triggerAt"], unique = true),
    ],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
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

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    petId = petId,
    type = type,
    title = title,
    referenceId = referenceId,
    triggerAt = triggerAt,
    isCompleted = isCompleted,
    isSnoozed = isSnoozed,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    petId = petId,
    type = type,
    title = title,
    referenceId = referenceId,
    triggerAt = triggerAt,
    isCompleted = isCompleted,
    isSnoozed = isSnoozed,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    isDeleted = isDeleted,
)
