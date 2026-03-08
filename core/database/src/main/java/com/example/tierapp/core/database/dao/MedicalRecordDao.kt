package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tierapp.core.database.entity.MedicalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {

    @Query("SELECT * FROM medical_record WHERE petId = :petId AND isDeleted = 0 ORDER BY date DESC")
    fun getByPetId(petId: String): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_record WHERE petId = :petId AND type = :type AND isDeleted = 0 ORDER BY date DESC")
    fun getByPetIdAndType(petId: String, type: String): Flow<List<MedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecordEntity)

    @Update
    suspend fun update(record: MedicalRecordEntity)

    @Query("UPDATE medical_record SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun softDelete(id: String, updatedAtMilli: Long)
}
