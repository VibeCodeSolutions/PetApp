package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.network.firestore.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeFirestoreDataSource : FirestoreDataSource {

    val pushedPets = mutableListOf<Pet>()
    val pushedPhotos = mutableListOf<PetPhoto>()
    val remotePets = mutableListOf<Pet>()
    val remotePhotos = mutableListOf<PetPhoto>()
    var shouldThrow = false

    /** Kontrollierbar für Tests: emit() um einen Snapshot zu simulieren. */
    val petSnapshotFlow = MutableSharedFlow<List<Pet>>(replay = 1)
    val photoSnapshotFlow = MutableSharedFlow<List<PetPhoto>>(replay = 1)

    override suspend fun pushPets(familyId: String, pets: List<Pet>) {
        if (shouldThrow) throw RuntimeException("Firestore error")
        pushedPets.addAll(pets)
    }

    override suspend fun pushPhotos(familyId: String, photos: List<PetPhoto>) {
        if (shouldThrow) throw RuntimeException("Firestore error")
        pushedPhotos.addAll(photos)
    }

    override suspend fun getPetsModifiedSince(familyId: String, sinceMillis: Long): List<Pet> {
        if (shouldThrow) throw RuntimeException("Firestore error")
        return remotePets.filter { it.updatedAt.toEpochMilli() > sinceMillis }
    }

    override suspend fun getPhotosModifiedSince(familyId: String, sinceMillis: Long): List<PetPhoto> {
        if (shouldThrow) throw RuntimeException("Firestore error")
        return remotePhotos.filter { it.updatedAt.toEpochMilli() > sinceMillis }
    }

    override fun observePets(familyId: String): Flow<List<Pet>> = petSnapshotFlow

    override fun observePhotos(familyId: String): Flow<List<PetPhoto>> = photoSnapshotFlow
}
