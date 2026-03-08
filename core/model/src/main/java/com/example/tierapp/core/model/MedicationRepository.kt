package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getByPetId(petId: String): Flow<List<Medication>>
    fun getActiveMedications(): Flow<List<Medication>>
    suspend fun getActiveList(): List<Medication>
    suspend fun insert(medication: Medication): Result<Unit>
    suspend fun update(medication: Medication): Result<Unit>
    suspend fun updateStock(id: String, newStock: Float): Result<Unit>
    suspend fun softDelete(id: String): Result<Unit>
}
