package com.example.tierapp.core.sync

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.network.auth.AuthRepository
import com.example.tierapp.core.network.firestore.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lifecycle-aware Realtime-Sync via Firestore SnapshotListener.
 *
 * Aktiv: ON_START bis ON_STOP (App im Vordergrund).
 * Inaktiv: ON_STOP — Firestore-Listener wird über awaitClose automatisch abgebaut.
 *
 * familyId wird aus Room (FamilyDao) bezogen — kein uid-Fallback mehr.
 * Datenfluss: Firestore → SyncEngine.applyRemoteSnapshot() → Room → UI (Flow)
 */
@Singleton
class RealtimeSyncObserver @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val syncEngine: SyncEngine,
    private val authRepository: AuthRepository,
    private val familyDao: FamilyDao,
    @ApplicationScope private val externalScope: CoroutineScope,
) : DefaultLifecycleObserver {

    private var observerJob: Job? = null

    fun register(owner: LifecycleOwner = ProcessLifecycleOwner.get()) {
        owner.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (authRepository.currentUser() == null) {
            Log.d(TAG, "onStart: kein User eingeloggt, kein Listener gestartet")
            return
        }

        observerJob?.cancel()
        observerJob = externalScope.launch {
            val familyId = familyDao.getCurrentFamilyDirect()?.id
            if (familyId == null) {
                Log.d(TAG, "onStart: keine Familie gefunden, kein Listener gestartet")
                return@launch
            }

            launch {
                firestoreDataSource.observePets(familyId).collect { pets ->
                    Log.d(TAG, "Snapshot: ${pets.size} Pets empfangen")
                    syncEngine.applyRemoteSnapshot(pets = pets)
                }
            }
            launch {
                firestoreDataSource.observePhotos(familyId).collect { photos ->
                    Log.d(TAG, "Snapshot: ${photos.size} Photos empfangen")
                    syncEngine.applyRemoteSnapshot(photos = photos)
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "onStop: Firestore-Listener wird abgebaut")
        observerJob?.cancel()
        observerJob = null
    }

    companion object {
        private const val TAG = "RealtimeSyncObserver"
    }
}
