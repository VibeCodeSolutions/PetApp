package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface PetPhotoRepository {
    fun getByPetId(petId: String): Flow<List<PetPhoto>>
    suspend fun insert(photo: PetPhoto)
    suspend fun delete(id: String)
}
