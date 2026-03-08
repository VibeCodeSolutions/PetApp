package com.example.tierapp.core.sync

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.network.auth.AuthRepository
import com.example.tierapp.core.network.firestore.FamilyFirestoreDataSource
import com.example.tierapp.core.network.firestore.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lifecycle-aware Realtime-Sync via Firestore SnapshotListener.
 *
 * Aktiv: ON_START bis ON_STOP (App im Vordergrund).
 * Inaktiv: ON_STOP — Firestore-Listener wird über awaitClose automatisch abgebaut.
 *
 * Synchronisiert: Pets, Photos UND FamilyMembers.
 * Alle collect-Blöcke sind gegen Exceptions abgesichert (kein Silent-Crash).
 */
@Singleton
class RealtimeSyncObserver @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val familyFirestoreDataSource: FamilyFirestoreDataSource,
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
                var retryDelayMs = 1_000L
                while (true) {
                    val result = runCatching {
                        firestoreDataSource.observePets(familyId).collect { pets ->
                            retryDelayMs = 1_000L
                            Log.d(TAG, "Snapshot: ${pets.size} Pets empfangen")
                            syncEngine.applyRemoteSnapshot(pets = pets)
                        }
                    }
                    if (result.isFailure) {
                        Log.e(TAG, "Fehler im Pets-Listener, retry in ${retryDelayMs}ms", result.exceptionOrNull())
                        delay(retryDelayMs)
                        retryDelayMs = (retryDelayMs * 2).coerceAtMost(60_000L)
                    } else break
                }
            }

            launch {
                var retryDelayMs = 1_000L
                while (true) {
                    val result = runCatching {
                        firestoreDataSource.observePhotos(familyId).collect { photos ->
                            retryDelayMs = 1_000L
                            Log.d(TAG, "Snapshot: ${photos.size} Photos empfangen")
                            syncEngine.applyRemoteSnapshot(photos = photos)
                        }
                    }
                    if (result.isFailure) {
                        Log.e(TAG, "Fehler im Photos-Listener, retry in ${retryDelayMs}ms", result.exceptionOrNull())
                        delay(retryDelayMs)
                        retryDelayMs = (retryDelayMs * 2).coerceAtMost(60_000L)
                    } else break
                }
            }

            // Mitglieder-Sync — hält beide Geräte aktuell wenn jemand beitritt
            launch {
                var retryDelayMs = 1_000L
                while (true) {
                    val result = runCatching {
                        familyFirestoreDataSource.observeMembers(familyId).collect { members ->
                            retryDelayMs = 1_000L
                            Log.d(TAG, "Snapshot: ${members.size} Members empfangen")
                            members.forEach { member ->
                                familyDao.insertMember(member.toEntity())
                            }
                        }
                    }
                    if (result.isFailure) {
                        Log.e(TAG, "Fehler im Members-Listener, retry in ${retryDelayMs}ms", result.exceptionOrNull())
                        delay(retryDelayMs)
                        retryDelayMs = (retryDelayMs * 2).coerceAtMost(60_000L)
                    } else break
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
