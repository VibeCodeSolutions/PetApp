package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.VaccinationDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Vaccination
import com.example.tierapp.core.model.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

internal class VaccinationRepositoryImpl @Inject constructor(
    private val dao: VaccinationDao,
) : VaccinationRepository {

    override fun getByPetId(petId: String): Flow<List<Vaccination>> =
        dao.getByPetId(petId).map { list -> list.map { it.toDomain() } }

    override fun getUpcoming(daysAhead: Int): Flow<List<Vaccination>> {
        val maxEpochDay = LocalDate.now().plusDays(daysAhead.toLong()).toEpochDay()
        return dao.getUpcoming(maxEpochDay).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insert(vaccination: Vaccination): Result<Unit> =
        runCatching { dao.insert(vaccination.toEntity()) }

    override suspend fun update(vaccination: Vaccination): Result<Unit> =
        runCatching { dao.update(vaccination.toEntity()) }

    override suspend fun softDelete(id: String): Result<Unit> =
        runCatching { dao.softDelete(id, Instant.now().toEpochMilli()) }
}
