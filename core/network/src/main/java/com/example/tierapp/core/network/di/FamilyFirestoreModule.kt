package com.example.tierapp.core.network.di

import com.example.tierapp.core.network.firestore.FamilyFirestoreDataSource
import com.example.tierapp.core.network.firestore.FamilyFirestoreDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FamilyFirestoreModule {

    @Binds
    @Singleton
    internal abstract fun bindFamilyFirestoreDataSource(
        impl: FamilyFirestoreDataSourceImpl,
    ): FamilyFirestoreDataSource
}
