package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface VaccinationRepository {
    fun getByPetId(petId: String): Flow<List<Vaccination>>
    fun getUpcoming(daysAhead: Int): Flow<List<Vaccination>>
    suspend fun insert(vaccination: Vaccination): Result<Unit>
    suspend fun update(vaccination: Vaccination): Result<Unit>
    suspend fun softDelete(id: String): Result<Unit>
}
