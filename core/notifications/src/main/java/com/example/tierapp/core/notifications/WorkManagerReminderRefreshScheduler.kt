package com.example.tierapp.core.notifications

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import javax.inject.Inject

class WorkManagerReminderRefreshScheduler @Inject constructor(
    private val workManager: WorkManager,
) : ReminderRefreshScheduler {

    override fun scheduleOneTimeRefresh() {
        workManager.enqueueUniqueWork(
            ReminderRefreshWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderRefreshWorker>().build(),
        )
    }
}
