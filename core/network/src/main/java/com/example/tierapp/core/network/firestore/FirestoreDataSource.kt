package com.example.tierapp.core.network.firestore

import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import kotlinx.coroutines.flow.Flow

interface FirestoreDataSource {

    suspend fun pushPets(familyId: String, pets: List<Pet>)

    suspend fun pushPhotos(familyId: String, photos: List<PetPhoto>)

    suspend fun getPetsModifiedSince(familyId: String, sinceMillis: Long): List<Pet>

    suspend fun getPhotosModifiedSince(familyId: String, sinceMillis: Long): List<PetPhoto>

    /** Realtime-Flow: emittiert bei jeder Firestore-Änderung alle Pets der Familie. */
    fun observePets(familyId: String): Flow<List<Pet>>

    /** Realtime-Flow: emittiert bei jeder Firestore-Änderung alle Photos der Familie. */
    fun observePhotos(familyId: String): Flow<List<PetPhoto>>
}
