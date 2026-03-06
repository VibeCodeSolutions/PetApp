package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tierapp.core.database.entity.PetPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetPhotoDao {

    @Query("SELECT * FROM pet_photo WHERE petId = :petId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getByPetId(petId: String): Flow<List<PetPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PetPhotoEntity)

    @Query("UPDATE pet_photo SET isDeleted = 1, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun softDelete(id: String)
}
