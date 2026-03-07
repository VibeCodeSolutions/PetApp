package com.example.tierapp.core.sync.di

import android.content.Context
import androidx.work.WorkManager
import com.example.tierapp.core.sync.ApplicationScope
import com.example.tierapp.core.sync.SharedPrefsSyncPreferences
import com.example.tierapp.core.sync.SyncPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncPreferences(impl: SharedPrefsSyncPreferences): SyncPreferences

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)

        /** Prozess-weiter Coroutine-Scope — überlebt Activity-Rotationen. */
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
