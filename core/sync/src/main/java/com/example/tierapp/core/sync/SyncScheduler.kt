package com.example.tierapp.core.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val uploadConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun requestImmediateSync(familyId: String? = null) {
        val data = familyId?.let { workDataOf(SyncWorker.KEY_FAMILY_ID to it) }
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .apply { if (data != null) setInputData(data) }
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_ONETIME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun schedulePhotoUpload(familyId: String? = null) {
        val data = familyId?.let { workDataOf(PhotoUploadWorker.KEY_FAMILY_ID to it) }
        val request = OneTimeWorkRequestBuilder<PhotoUploadWorker>()
            .setConstraints(uploadConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
            .apply { if (data != null) setInputData(data) }
            .build()

        workManager.enqueueUniqueWork(
            PhotoUploadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelAll() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_ONETIME)
        workManager.cancelUniqueWork(PhotoUploadWorker.WORK_NAME)
    }
}
