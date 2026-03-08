package com.example.tierapp.core.database.di

import com.example.tierapp.core.database.repository.VaccinationRepositoryImpl
import com.example.tierapp.core.model.VaccinationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class VaccinationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVaccinationRepository(impl: VaccinationRepositoryImpl): VaccinationRepository
}
