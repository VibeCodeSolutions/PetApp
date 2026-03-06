package com.example.tierapp.core.media.di

import com.example.tierapp.core.media.ThumbnailManager
import com.example.tierapp.core.media.ThumbnailManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindThumbnailManager(impl: ThumbnailManagerImpl): ThumbnailManager
}
