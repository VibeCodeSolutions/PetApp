package com.example.tierapp.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.notifications.ReminderRefreshScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEngine: SyncEngine,
    private val firebaseAuth: FirebaseAuth,
    private val familyDao: FamilyDao,
    private val reminderRefreshScheduler: ReminderRefreshScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (firebaseAuth.currentUser == null) {
            Log.w(TAG, "Not authenticated, skipping sync")
            return Result.success()
        }

        val familyId = familyDao.getCurrentFamilyDirect()?.id
        if (familyId == null) {
            Log.w(TAG, "No family found, skipping sync")
            return Result.success()
        }

        return when (val result = syncEngine.sync(familyId)) {
            is SyncResult.Success -> {
                Log.d(TAG, "Sync completed successfully for family $familyId")
                reminderRefreshScheduler.scheduleOneTimeRefresh()
                Result.success()
            }
            is SyncResult.PermanentError -> {
                Log.e(TAG, "Permanent sync error, giving up", result.cause)
                Result.failure()
            }
            is SyncResult.TransientError -> {
                if (runAttemptCount >= MAX_RETRIES) {
                    Log.e(TAG, "Max retries ($MAX_RETRIES) reached after transient error, giving up", result.cause)
                    Result.failure()
                } else {
                    Log.w(TAG, "Transient sync error, attempt $runAttemptCount/$MAX_RETRIES, retrying", result.cause)
                    Result.retry()
                }
            }
        }
    }

    companion object {
        const val TAG = "SyncWorker"
        const val KEY_FAMILY_ID = "family_id"
        const val WORK_NAME_PERIODIC = "sync_periodic"
        const val WORK_NAME_ONETIME = "sync_onetime"
        private const val MAX_RETRIES = 3
    }
}
