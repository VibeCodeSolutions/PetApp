package com.example.tierapp.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tierapp.core.database.dao.FamilyDao
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PhotoUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val photoUploadEngine: PhotoUploadEngine,
    private val firebaseAuth: FirebaseAuth,
    private val familyDao: FamilyDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (firebaseAuth.currentUser == null) {
            Log.w(TAG, "Not authenticated, skipping photo upload")
            return Result.success()
        }

        val familyId = familyDao.getCurrentFamilyDirect()?.id
        if (familyId == null) {
            Log.w(TAG, "No family found, skipping photo upload")
            return Result.success()
        }

        if (runAttemptCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries ($MAX_RETRIES) reached, giving up")
            return Result.failure()
        }

        val success = photoUploadEngine.uploadPending(familyId)
        return if (success) {
            Log.d(TAG, "Photo upload completed successfully for family $familyId")
            Result.success()
        } else {
            Log.w(TAG, "Photo upload failed, attempt $runAttemptCount/$MAX_RETRIES")
            Result.retry()
        }
    }

    companion object {
        const val TAG = "PhotoUploadWorker"
        const val KEY_FAMILY_ID = "family_id"
        const val WORK_NAME = "photo_upload"
        private const val MAX_RETRIES = 3
    }
}
