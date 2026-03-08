package com.example.tierapp.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tierapp.core.database.dao.MedicationDao
import com.example.tierapp.core.database.dao.VaccinationDao
import com.example.tierapp.core.database.entity.MedicationEntity
import com.example.tierapp.core.database.entity.VaccinationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class ReminderRefreshWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val vaccinationDao: VaccinationDao,
    private val medicationDao: MedicationDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting reminder refresh")

        val notifManager = NotificationManagerCompat.from(appContext)
        ensureChannelsExist(notifManager)
        cancelStaleReminders(notifManager)

        if (!hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping")
            return Result.success()
        }

        val todayEpochDay = LocalDate.now().toEpochDay()
        val upcomingVaccinations = vaccinationDao.getUpcomingList(
            maxEpochDay = todayEpochDay + VACCINATION_LOOKAHEAD_DAYS,
        )
        val lowStockMedications = medicationDao.getActiveList().filter { it.isLowStock() }

        upcomingVaccinations.forEach { notifManager.postVaccinationNotification(it) }
        lowStockMedications.forEach { notifManager.postMedicationNotification(it) }

        Log.d(
            TAG,
            "Reminder refresh done: ${upcomingVaccinations.size} vaccinations, " +
                "${lowStockMedications.size} low-stock medications notified",
        )
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun cancelStaleReminders(manager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.activeNotifications
                .filter { it.tag == NOTIF_TAG }
                .forEach { manager.cancel(NOTIF_TAG, it.id) }
        }
    }

    private fun ensureChannelsExist(manager: NotificationManagerCompat) {
        val vacChannel = NotificationChannel(
            CHANNEL_VACCINATION,
            "Impftermin-Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Benachrichtigungen für bevorstehende Impftermine" }

        val medChannel = NotificationChannel(
            CHANNEL_MEDICATION,
            "Medikamenten-Bestand",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Benachrichtigungen bei niedrigem Medikamenten-Bestand" }

        manager.createNotificationChannel(vacChannel)
        manager.createNotificationChannel(medChannel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun NotificationManagerCompat.postVaccinationNotification(vac: VaccinationEntity) {
        val daysUntilDue = (vac.nextDueDate!!.toEpochDay() - LocalDate.now().toEpochDay()).toInt()
        val notification = NotificationCompat.Builder(appContext, CHANNEL_VACCINATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Impftermin: ${vac.name}")
            .setContentText("Fällig in $daysUntilDue Tag(en)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notify(NOTIF_TAG, vac.id.hashCode(), notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun NotificationManagerCompat.postMedicationNotification(med: MedicationEntity) {
        val daysLeft =
            if (med.dailyConsumption > 0) (med.currentStock / med.dailyConsumption).toInt() else 0
        val notification = NotificationCompat.Builder(appContext, CHANNEL_MEDICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Niedriger Bestand: ${med.name}")
            .setContentText("Noch ca. $daysLeft Tag(e) Vorrat verbleibend")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notify(NOTIF_TAG, med.id.hashCode(), notification)
    }

    private fun MedicationEntity.isLowStock(): Boolean =
        dailyConsumption > 0f && currentStock < dailyConsumption * LOW_STOCK_THRESHOLD_DAYS

    companion object {
        const val TAG = "ReminderRefreshWorker"
        const val WORK_NAME = "reminder_refresh_onetime"
        private const val NOTIF_TAG = "health_reminder"
        private const val CHANNEL_VACCINATION = "channel_vaccination_reminder"
        private const val CHANNEL_MEDICATION = "channel_medication_stock"
        private const val VACCINATION_LOOKAHEAD_DAYS = 30L
        private const val LOW_STOCK_THRESHOLD_DAYS = 5f
    }
}
