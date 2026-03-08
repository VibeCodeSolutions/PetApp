package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.ReminderDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Reminder
import com.example.tierapp.core.model.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

internal class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {

    override fun getPendingReminders(): Flow<List<Reminder>> =
        dao.getPendingReminders().map { list -> list.map { it.toDomain() } }

    override suspend fun getDueList(nowMilli: Long): List<Reminder> =
        dao.getDueList(nowMilli).map { it.toDomain() }

    override suspend fun insertIfNotExists(reminder: Reminder): Result<Unit> =
        runCatching { dao.insertIfNotExists(reminder.toEntity()) }

    override suspend fun markCompleted(id: String): Result<Unit> =
        runCatching { dao.markCompleted(id, Instant.now().toEpochMilli()) }

    override suspend fun snooze(id: String, untilMilli: Long): Result<Unit> =
        runCatching { dao.snooze(id, untilMilli, Instant.now().toEpochMilli()) }

    override suspend fun softDelete(id: String): Result<Unit> =
        runCatching { dao.softDelete(id, Instant.now().toEpochMilli()) }
}
