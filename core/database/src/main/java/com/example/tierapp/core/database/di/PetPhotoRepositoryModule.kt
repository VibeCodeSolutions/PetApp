package com.example.tierapp.core.database.di

import com.example.tierapp.core.database.repository.PetPhotoRepositoryImpl
import com.example.tierapp.core.model.PetPhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetPhotoRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPetPhotoRepository(impl: PetPhotoRepositoryImpl): PetPhotoRepository
}
