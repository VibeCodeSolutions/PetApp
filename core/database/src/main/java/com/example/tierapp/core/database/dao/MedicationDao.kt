package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tierapp.core.database.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medication WHERE petId = :petId AND isDeleted = 0 ORDER BY name ASC")
    fun getByPetId(petId: String): Flow<List<MedicationEntity>>

    /** Alle aktiven (nicht gelöschten) Medikamente – für Dashboard und Worker. */
    @Query("SELECT * FROM medication WHERE isDeleted = 0 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    /** Suspend variant for Workers. */
    @Query("SELECT * FROM medication WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getActiveList(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity)

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("UPDATE medication SET currentStock = :newStock, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Float, updatedAtMilli: Long)

    @Query("UPDATE medication SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun softDelete(id: String, updatedAtMilli: Long)
}
