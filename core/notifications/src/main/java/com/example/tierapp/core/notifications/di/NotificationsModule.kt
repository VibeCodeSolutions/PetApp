package com.example.tierapp.core.notifications.di

import com.example.tierapp.core.notifications.ReminderRefreshScheduler
import com.example.tierapp.core.notifications.WorkManagerReminderRefreshScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}
