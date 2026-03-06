package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getAll(): Flow<List<Pet>>
    fun getById(id: String): Flow<Pet?>
    suspend fun insert(pet: Pet)
    suspend fun update(pet: Pet)
    suspend fun delete(id: String)
}
