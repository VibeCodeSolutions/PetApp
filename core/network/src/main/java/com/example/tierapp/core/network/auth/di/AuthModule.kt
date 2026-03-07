// core/network/src/main/java/com/example/tierapp/core/network/auth/di/AuthModule.kt
package com.example.tierapp.core.network.auth.di

import com.example.tierapp.core.network.auth.AuthRepository
import com.example.tierapp.core.network.auth.FirebaseAuthRepository
import com.example.tierapp.core.network.auth.datasource.AuthDataSource
import com.example.tierapp.core.network.auth.datasource.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    internal abstract fun bindAuthDataSource(impl: FirebaseAuthDataSource): AuthDataSource

    @Binds
    @Singleton
    internal abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
