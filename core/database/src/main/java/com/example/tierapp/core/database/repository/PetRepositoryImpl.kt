package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val dao: PetDao,
) : PetRepository {

    override fun getAll(): Flow<List<Pet>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getById(id: String): Flow<Pet?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun insert(pet: Pet): Unit = dao.insert(pet.toEntity())

    override suspend fun update(pet: Pet): Unit = dao.update(pet.toEntity())

    override suspend fun delete(id: String): Unit =
        dao.softDelete(id, Instant.now().toEpochMilli())
}
