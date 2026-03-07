package com.example.tierapp.feature.family.di

import com.example.tierapp.core.model.FamilyRepository
import com.example.tierapp.feature.family.FamilyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FamilyModule {

    @Binds
    @Singleton
    internal abstract fun bindFamilyRepository(impl: FamilyRepositoryImpl): FamilyRepository
}
