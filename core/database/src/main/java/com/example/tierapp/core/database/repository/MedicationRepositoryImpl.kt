package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.MedicationDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Medication
import com.example.tierapp.core.model.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

internal class MedicationRepositoryImpl @Inject constructor(
    private val dao: MedicationDao,
) : MedicationRepository {

    override fun getByPetId(petId: String): Flow<List<Medication>> =
        dao.getByPetId(petId).map { list -> list.map { it.toDomain() } }

    override fun getActiveMedications(): Flow<List<Medication>> =
        dao.getActiveMedications().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveList(): List<Medication> =
        dao.getActiveList().map { it.toDomain() }

    override suspend fun insert(medication: Medication): Result<Unit> =
        runCatching { dao.insert(medication.toEntity()) }

    override suspend fun update(medication: Medication): Result<Unit> =
        runCatching { dao.update(medication.toEntity()) }

    override suspend fun updateStock(id: String, newStock: Float): Result<Unit> =
        runCatching { dao.updateStock(id, newStock, Instant.now().toEpochMilli()) }

    override suspend fun softDelete(id: String): Result<Unit> =
        runCatching { dao.softDelete(id, Instant.now().toEpochMilli()) }
}
