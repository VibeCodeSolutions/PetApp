package com.example.tierapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.tierapp.core.database.TierappDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTierappDatabase(
        @ApplicationContext context: Context,
    ): TierappDatabase = Room.databaseBuilder(
        context,
        TierappDatabase::class.java,
        "tierapp.db",
    ).build()
}
