package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tierapp.core.database.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pet WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pet WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): Flow<PetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity)

    @Update
    suspend fun update(pet: PetEntity)

    /** Soft-Delete: markiert als geloescht und setzt syncStatus auf PENDING fuer den naechsten Sync. */
    @Query("UPDATE pet SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAtMilli WHERE id = :id")
    suspend fun softDelete(id: String, updatedAtMilli: Long)
}
