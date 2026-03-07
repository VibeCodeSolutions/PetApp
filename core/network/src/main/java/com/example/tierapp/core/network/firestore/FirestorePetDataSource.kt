package com.example.tierapp.core.network.firestore

import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirestorePetDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : FirestoreDataSource {

    override suspend fun pushPets(familyId: String, pets: List<Pet>) {
        if (pets.isEmpty()) return
        pets.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch: WriteBatch = firestore.batch()
            for (pet in chunk) {
                val ref = firestore
                    .collection("families").document(familyId)
                    .collection("pets").document(pet.id)
                batch.set(ref, pet.toFirestoreMap())
            }
            batch.commit().await()
        }
    }

    override suspend fun pushPhotos(familyId: String, photos: List<PetPhoto>) {
        if (photos.isEmpty()) return
        photos.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch: WriteBatch = firestore.batch()
            for (photo in chunk) {
                val ref = firestore
                    .collection("families").document(familyId)
                    .collection("pets").document(photo.petId)
                    .collection("photos").document(photo.id)
                batch.set(ref, photo.toFirestoreMap())
            }
            batch.commit().await()
        }
    }

    override suspend fun getPetsModifiedSince(
        familyId: String,
        sinceMillis: Long,
    ): List<Pet> {
        val snapshot = firestore
            .collection("families").document(familyId)
            .collection("pets")
            .whereGreaterThan("updatedAt", sinceMillis)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toPet(doc.id)
        }
    }

    override suspend fun getPhotosModifiedSince(
        familyId: String,
        sinceMillis: Long,
    ): List<PetPhoto> {
        val snapshot = firestore
            .collectionGroup("photos")
            .whereGreaterThan("updatedAt", sinceMillis)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val petId = doc.reference.parent.parent?.id ?: return@mapNotNull null
            val familyIdFromPath = doc.reference.parent.parent?.parent?.parent?.id
            if (familyIdFromPath != familyId) return@mapNotNull null
            doc.data?.toPetPhoto(doc.id, petId)
        }
    }

    override fun observePets(familyId: String): Flow<List<Pet>> = callbackFlow {
        val ref = firestore
            .collection("families").document(familyId)
            .collection("pets")
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val pets = snapshot?.documents?.mapNotNull { doc -> doc.data?.toPet(doc.id) } ?: emptyList()
            trySend(pets)
        }
        awaitClose { registration.remove() }
    }

    override fun observePhotos(familyId: String): Flow<List<PetPhoto>> = callbackFlow {
        val query = firestore.collectionGroup("photos")
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val photos = snapshot?.documents?.mapNotNull { doc ->
                val petId = doc.reference.parent.parent?.id ?: return@mapNotNull null
                val familyIdFromPath = doc.reference.parent.parent?.parent?.parent?.id
                if (familyIdFromPath != familyId) return@mapNotNull null
                doc.data?.toPetPhoto(doc.id, petId)
            } ?: emptyList()
            trySend(photos)
        }
        awaitClose { registration.remove() }
    }

    companion object {
        private const val MAX_BATCH_SIZE = 500
    }
}

private fun Pet.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "birthDate" to birthDate?.toEpochDay(),
    "species" to species.name,
    "breed" to breed,
    "chipNumber" to chipNumber,
    "color" to color,
    "weightKg" to weightKg,
    "notes" to notes,
    "profilePhotoId" to profilePhotoId,
    "familyId" to familyId,
    "createdAt" to createdAt.toEpochMilli(),
    "updatedAt" to updatedAt.toEpochMilli(),
    "syncStatus" to SyncStatus.SYNCED.name,
    "isDeleted" to isDeleted,
)

private fun Map<String, Any?>.toPet(id: String): Pet? {
    return Pet(
        id = id,
        name = this["name"] as? String ?: return null,
        birthDate = (this["birthDate"] as? Long)?.let { LocalDate.ofEpochDay(it) },
        species = (this["species"] as? String)?.let {
            runCatching { PetSpecies.valueOf(it) }.getOrNull()
        } ?: PetSpecies.OTHER,
        breed = this["breed"] as? String,
        chipNumber = this["chipNumber"] as? String,
        color = this["color"] as? String,
        weightKg = (this["weightKg"] as? Number)?.toFloat(),
        notes = this["notes"] as? String,
        profilePhotoId = this["profilePhotoId"] as? String,
        familyId = this["familyId"] as? String,
        createdAt = Instant.ofEpochMilli(this["createdAt"] as? Long ?: 0L),
        updatedAt = Instant.ofEpochMilli(this["updatedAt"] as? Long ?: 0L),
        syncStatus = SyncStatus.SYNCED,
        isDeleted = this["isDeleted"] as? Boolean ?: false,
    )
}

private fun PetPhoto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "petId" to petId,
    "remoteOriginalUrl" to remoteOriginalUrl,
    "remoteThumbSmallUrl" to remoteThumbSmallUrl,
    "remoteThumbMediumUrl" to remoteThumbMediumUrl,
    "uploadStatus" to uploadStatus.name,
    "createdAt" to createdAt.toEpochMilli(),
    "updatedAt" to updatedAt.toEpochMilli(),
    "syncStatus" to SyncStatus.SYNCED.name,
    "isDeleted" to isDeleted,
)

private fun Map<String, Any?>.toPetPhoto(id: String, petId: String): PetPhoto? {
    return PetPhoto(
        id = id,
        petId = petId,
        originalPath = "",
        thumbSmallPath = null,
        thumbMediumPath = null,
        remoteOriginalUrl = this["remoteOriginalUrl"] as? String,
        remoteThumbSmallUrl = this["remoteThumbSmallUrl"] as? String,
        remoteThumbMediumUrl = this["remoteThumbMediumUrl"] as? String,
        uploadStatus = (this["uploadStatus"] as? String)?.let {
            runCatching { UploadStatus.valueOf(it) }.getOrNull()
        } ?: UploadStatus.UPLOADED,
        createdAt = Instant.ofEpochMilli(this["createdAt"] as? Long ?: 0L),
        updatedAt = Instant.ofEpochMilli(this["updatedAt"] as? Long ?: 0L),
        syncStatus = SyncStatus.SYNCED,
        isDeleted = this["isDeleted"] as? Boolean ?: false,
    )
}
