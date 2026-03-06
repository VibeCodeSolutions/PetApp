package com.example.tierapp.core.database.di

import com.example.tierapp.core.database.repository.PetRepositoryImpl
import com.example.tierapp.core.model.PetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository
}
