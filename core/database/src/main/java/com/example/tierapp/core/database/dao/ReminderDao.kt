package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tierapp.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    /** IGNORE nutzt den Unique-Index (referenceId, triggerAt) als atomaren Duplicate-Guard. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(reminder: ReminderEntity)

    @Query(
        """
        SELECT * FROM reminder
        WHERE isDeleted = 0 AND isCompleted = 0 AND triggerAt <= :nowMilli
        ORDER BY triggerAt ASC
        """
    )
    suspend fun getDueList(nowMilli: Long): List<ReminderEntity>

    @Query(
        """
        SELECT * FROM reminder
        WHERE isDeleted = 0 AND isCompleted = 0 AND isSnoozed = 0
        ORDER BY triggerAt ASC
        """
    )
    fun getPendingReminders(): Flow<List<ReminderEntity>>

    @Query("UPDATE reminder SET isCompleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun markCompleted(id: String, updatedAtMilli: Long)

    @Query("UPDATE reminder SET isSnoozed = 1, triggerAt = :untilMilli, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun snooze(id: String, untilMilli: Long, updatedAtMilli: Long)

    @Query("UPDATE reminder SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun softDelete(id: String, updatedAtMilli: Long)
}
