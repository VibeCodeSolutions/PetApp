package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.entity.PetEntity
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePetDao : PetDao {

    private val store = mutableMapOf<String, PetEntity>()
    private val flow = MutableStateFlow<List<PetEntity>>(emptyList())

    fun insertForTest(pet: Pet) {
        store[pet.id] = pet.toEntity()
    }

    fun getDomainById(id: String): Pet? = store[id]?.toDomain()

    override fun getAll(): Flow<List<PetEntity>> =
        flow.map { store.values.filter { !it.isDeleted }.toList() }

    override fun getById(id: String): Flow<PetEntity?> =
        flow.map { store[id]?.takeIf { !it.isDeleted } }

    override suspend fun insert(pet: PetEntity) {
        store[pet.id] = pet
    }

    override suspend fun update(pet: PetEntity) {
        store[pet.id] = pet
    }

    override suspend fun softDelete(id: String, updatedAtMilli: Long) {
        store[id]?.let { store[id] = it.copy(isDeleted = true, syncStatus = SyncStatus.PENDING) }
    }

    override suspend fun getPending(): List<PetEntity> =
        store.values.filter { it.syncStatus == SyncStatus.PENDING }.toList()

    override suspend fun getByIdDirect(id: String): PetEntity? = store[id]

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        store[id]?.let { store[id] = it.copy(syncStatus = status) }
    }

    override suspend fun upsert(pet: PetEntity) {
        store[pet.id] = pet
    }
}
