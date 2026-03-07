package com.example.tierapp.core.network.di

import com.example.tierapp.core.network.firestore.FirestoreDataSource
import com.example.tierapp.core.network.firestore.FirestorePetDataSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreModule {

    @Binds
    @Singleton
    internal abstract fun bindFirestoreDataSource(impl: FirestorePetDataSource): FirestoreDataSource

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
