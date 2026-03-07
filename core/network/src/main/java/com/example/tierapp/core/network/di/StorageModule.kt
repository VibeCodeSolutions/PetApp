package com.example.tierapp.core.network.di

import com.example.tierapp.core.network.storage.FirebaseStorageDataSource
import com.example.tierapp.core.network.storage.StorageDataSource
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    internal abstract fun bindStorageDataSource(impl: FirebaseStorageDataSource): StorageDataSource

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    }
}
