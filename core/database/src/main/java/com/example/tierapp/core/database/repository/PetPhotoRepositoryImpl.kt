package com.example.tierapp.core.database.repository

import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetPhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetPhotoRepositoryImpl @Inject constructor(
    private val dao: PetPhotoDao,
) : PetPhotoRepository {

    override fun getByPetId(petId: String): Flow<List<PetPhoto>> =
        dao.getByPetId(petId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(photo: PetPhoto): Unit = dao.insert(photo.toEntity())

    override suspend fun delete(id: String): Unit = dao.softDelete(id)
}
