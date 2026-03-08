package com.example.tierapp.core.database.di

import com.example.tierapp.core.database.repository.ReminderRepositoryImpl
import com.example.tierapp.core.model.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReminderRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
