package com.example.tierapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.tierapp.core.database.TierappDatabase
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.migration.MIGRATION_1_2
import com.example.tierapp.core.database.migration.MIGRATION_2_3
import com.example.tierapp.core.database.migration.MIGRATION_3_4
import com.example.tierapp.core.database.migration.MIGRATION_4_5
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
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()

    @Provides
    fun providePetDao(db: TierappDatabase): PetDao = db.petDao()

    @Provides
    fun providePetPhotoDao(db: TierappDatabase): PetPhotoDao = db.petPhotoDao()

    @Provides
    fun provideFamilyDao(db: TierappDatabase): FamilyDao = db.familyDao()
}
