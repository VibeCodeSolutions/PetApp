package com.example.tierapp.core.notifications.di

import android.content.Context
import androidx.work.WorkManager
import com.example.tierapp.core.notifications.ReminderRefreshScheduler
import com.example.tierapp.core.notifications.WorkManagerReminderRefreshScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindReminderRefreshScheduler(
        impl: WorkManagerReminderRefreshScheduler,
    ): ReminderRefreshScheduler

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
