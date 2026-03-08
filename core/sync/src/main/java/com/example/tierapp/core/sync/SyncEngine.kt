package com.example.tierapp.core.sync

import android.util.Log
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.Pet
import com.example.tierapp.core.model.PetPhoto
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.network.firestore.FirestoreDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject

class SyncEngine @Inject constructor(
    private val petDao: PetDao,
    private val petPhotoDao: PetPhotoDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncResolver: SyncResolver,
    private val syncPreferences: SyncPreferences,
) {

    /**
     * Fuehrt einen vollstaendigen Sync-Zyklus aus:
     * 1. Pull: Aenderungen seit letztem Sync von Firestore holen und mergen
     * 2. Push: Alle PENDING-Entities zu Firestore (in Chunks <= FIRESTORE_BATCH_LIMIT)
     *
     * Pull-first stellt sicher, dass lokale PENDING-Entities mit neuerem
     * Timestamp nicht faelschlicherweise auf SYNCED gesetzt werden, bevor
     * der Konflikt-Resolver sie gegen Remote-Aenderungen pruefen kann.
     *
     * @return [SyncResult.Success], [SyncResult.TransientError] oder [SyncResult.PermanentError]
     */
    suspend fun sync(familyId: String): SyncResult {
        return try {
            pull(familyId)
            push(familyId)
            syncPreferences.lastSyncTimestamp = System.currentTimeMillis()
            SyncResult.Success
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                FirebaseFirestoreException.Code.UNAUTHENTICATED,
                FirebaseFirestoreException.Code.INVALID_ARGUMENT,
                FirebaseFirestoreException.Code.NOT_FOUND -> {
                    Log.e(TAG, "Permanent sync error [${e.code}]", e)
                    SyncResult.PermanentError(e)
                }
                else -> {
                    // UNAVAILABLE, DEADLINE_EXCEEDED, ABORTED, RESOURCE_EXHAUSTED = transient
                    Log.w(TAG, "Transient sync error [${e.code}], will retry", e)
                    SyncResult.TransientError(e)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Transient sync error, will retry", e)
            SyncResult.TransientError(e)
        }
    }

    private suspend fun push(familyId: String) {
        // Push pending pets in safe chunks (Firestore batch limit)
        val pendingPets = petDao.getPending()
        if (pendingPets.isNotEmpty()) {
            pendingPets.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
                firestoreDataSource.pushPets(familyId, chunk.map { it.toDomain() })
            }
            for (pet in pendingPets) {
                val current = petDao.getByIdDirect(pet.id)
                if (current?.syncStatus == SyncStatus.PENDING
                    && current.updatedAt == pet.updatedAt
                ) {
                    petDao.updateSyncStatus(pet.id, SyncStatus.SYNCED)
                }
            }
        }

        // Push pending photos in safe chunks (Firestore batch limit)
        val pendingPhotos = petPhotoDao.getPending()
        if (pendingPhotos.isNotEmpty()) {
            pendingPhotos.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
                firestoreDataSource.pushPhotos(familyId, chunk.map { it.toDomain() })
            }
            for (photo in pendingPhotos) {
                val current = petPhotoDao.getByIdDirect(photo.id)
                if (current?.syncStatus == SyncStatus.PENDING
                    && current.updatedAt == photo.updatedAt
                ) {
                    petPhotoDao.updateSyncStatus(photo.id, SyncStatus.SYNCED)
                }
            }
        }
    }

    private suspend fun pull(familyId: String) {
        val since = syncPreferences.lastSyncTimestamp

        // Pull remote pets
        val remotePets = firestoreDataSource.getPetsModifiedSince(familyId, since)
        for (remotePet in remotePets) {
            mergeRemotePet(remotePet)
        }

        // Pull remote photos
        val remotePhotos = firestoreDataSource.getPhotosModifiedSince(familyId, since)
        for (remotePhoto in remotePhotos) {
            mergeRemotePhoto(remotePhoto)
        }
    }

    private suspend fun mergeRemotePet(remote: Pet) {
        val localEntity = petDao.getByIdDirect(remote.id)
        val localMeta = localEntity?.let {
            SyncMeta(it.updatedAt.toEpochMilli(), it.syncStatus, it.isDeleted)
        }
        val remoteMeta = SyncMeta(
            remote.updatedAt.toEpochMilli(), SyncStatus.SYNCED, remote.isDeleted,
        )

        when (syncResolver.resolve(localMeta, remoteMeta)) {
            SyncDecision.UseRemote, SyncDecision.DeleteLocal -> {
                petDao.upsert(remote.copy(syncStatus = SyncStatus.SYNCED).toEntity())
            }
            SyncDecision.UseLocal, SyncDecision.DeleteRemote -> {
                // Local wins - will be pushed in next cycle
            }
            SyncDecision.Skip -> { /* Already in sync */ }
        }
    }

    private suspend fun mergeRemotePhoto(remote: PetPhoto) {
        val localEntity = petPhotoDao.getByIdDirect(remote.id)
        val localMeta = localEntity?.let {
            SyncMeta(it.updatedAt.toEpochMilli(), it.syncStatus, it.isDeleted)
        }
        val remoteMeta = SyncMeta(
            remote.updatedAt.toEpochMilli(), SyncStatus.SYNCED, remote.isDeleted,
        )

        when (syncResolver.resolve(localMeta, remoteMeta)) {
            SyncDecision.UseRemote, SyncDecision.DeleteLocal -> {
                // Preserve local paths if they exist, use remote metadata
                val merged = if (localEntity != null) {
                    remote.copy(
                        originalPath = localEntity.originalPath,
                        thumbSmallPath = localEntity.thumbSmallPath,
                        thumbMediumPath = localEntity.thumbMediumPath,
                        syncStatus = SyncStatus.SYNCED,
                    )
                } else {
                    remote.copy(syncStatus = SyncStatus.SYNCED)
                }
                petPhotoDao.upsert(merged.toEntity())
            }
            SyncDecision.UseLocal, SyncDecision.DeleteRemote -> {
                // Local wins
            }
            SyncDecision.Skip -> { /* Already in sync */ }
        }
    }

    /**
     * Wendet einen Realtime-Snapshot (vom SnapshotListener) auf Room an.
     * Nutzt dieselbe Merge-Logik wie der Pull-Schritt des periodischen Syncs.
     */
    suspend fun applyRemoteSnapshot(
        pets: List<Pet> = emptyList(),
        photos: List<PetPhoto> = emptyList(),
    ) {
        for (pet in pets) mergeRemotePet(pet)
        for (photo in photos) mergeRemotePhoto(photo)
    }

    companion object {
        private const val TAG = "SyncEngine"

        /**
         * Sicheres Limit unterhalb des Firestore-Batch-Limits (500),
         * um Overhead bei gleichzeitigen Schreiboperationen zu vermeiden.
         */
        const val FIRESTORE_BATCH_LIMIT = 400
    }
}
