package com.example.tierapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.entity.PetEntity

/**
 * Zentrale Room-Datenbank der Tierapp.
 *
 * Jede Schema-Aenderung erfordert eine Migration in migration/Migrations.kt
 * und eine Erhoehung der version.
 *
 * Version 1: Leeres Schema (Sprint 1.3)
 * Version 2: Pet-Tabelle (Sprint 2.1)
 */
@Database(
    entities = [PetEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(TierappTypeConverters::class)
abstract class TierappDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
}
