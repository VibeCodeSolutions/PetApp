package com.example.tierapp.core.database.di

import com.example.tierapp.core.database.repository.MedicalRecordRepositoryImpl
import com.example.tierapp.core.model.MedicalRecordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MedicalRecordRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicalRecordRepository(impl: MedicalRecordRepositoryImpl): MedicalRecordRepository
}
