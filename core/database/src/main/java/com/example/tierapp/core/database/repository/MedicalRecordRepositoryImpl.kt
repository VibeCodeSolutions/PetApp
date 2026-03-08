package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.MedicalRecordDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.MedicalRecord
import com.example.tierapp.core.model.MedicalRecordRepository
import com.example.tierapp.core.model.MedicalRecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

internal class MedicalRecordRepositoryImpl @Inject constructor(
    private val dao: MedicalRecordDao,
) : MedicalRecordRepository {

    override fun getByPetId(petId: String): Flow<List<MedicalRecord>> =
        dao.getByPetId(petId).map { list -> list.map { it.toDomain() } }

    override fun getByPetIdAndType(petId: String, type: MedicalRecordType): Flow<List<MedicalRecord>> =
        dao.getByPetIdAndType(petId, type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(record: MedicalRecord): Result<Unit> =
        runCatching { dao.insert(record.toEntity()) }

    override suspend fun update(record: MedicalRecord): Result<Unit> =
        runCatching { dao.update(record.toEntity()) }

    override suspend fun softDelete(id: String): Result<Unit> =
        runCatching { dao.softDelete(id, Instant.now().toEpochMilli()) }
}
