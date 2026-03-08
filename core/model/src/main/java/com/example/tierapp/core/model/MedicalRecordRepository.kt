package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface MedicalRecordRepository {
    fun getByPetId(petId: String): Flow<List<MedicalRecord>>
    fun getByPetIdAndType(petId: String, type: MedicalRecordType): Flow<List<MedicalRecord>>
    suspend fun insert(record: MedicalRecord): Result<Unit>
    suspend fun update(record: MedicalRecord): Result<Unit>
    suspend fun softDelete(id: String): Result<Unit>
}
