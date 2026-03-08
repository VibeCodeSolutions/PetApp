package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getPendingReminders(): Flow<List<Reminder>>
    suspend fun getDueList(nowMilli: Long): List<Reminder>
    suspend fun insertIfNotExists(reminder: Reminder): Result<Unit>
    suspend fun markCompleted(id: String): Result<Unit>
    suspend fun snooze(id: String, untilMilli: Long): Result<Unit>
    suspend fun softDelete(id: String): Result<Unit>
}
