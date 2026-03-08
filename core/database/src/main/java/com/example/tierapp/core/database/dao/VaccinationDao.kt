package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tierapp.core.database.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {

    @Query("SELECT * FROM vaccination WHERE petId = :petId AND isDeleted = 0 ORDER BY dateAdministered DESC")
    fun getByPetId(petId: String): Flow<List<VaccinationEntity>>

    /** Upcoming vaccinations within [daysAhead] days from today (epoch-day). */
    @Query(
        """
        SELECT * FROM vaccination
        WHERE isDeleted = 0 AND nextDueDate IS NOT NULL
          AND nextDueDate <= :maxEpochDay
        ORDER BY nextDueDate ASC
        """
    )
    fun getUpcoming(maxEpochDay: Long): Flow<List<VaccinationEntity>>

    /** Suspend variant for use in Workers. */
    @Query(
        """
        SELECT * FROM vaccination
        WHERE isDeleted = 0 AND nextDueDate IS NOT NULL
          AND nextDueDate <= :maxEpochDay
        ORDER BY nextDueDate ASC
        """
    )
    suspend fun getUpcomingList(maxEpochDay: Long): List<VaccinationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: VaccinationEntity)

    @Update
    suspend fun update(vaccination: VaccinationEntity)

    @Query("UPDATE vaccination SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun softDelete(id: String, updatedAtMilli: Long)
}
