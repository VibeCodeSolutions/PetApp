package com.example.tierapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Zentrale Room-Datenbank der Tierapp.
 *
 * Entities werden in Phase 2/3 ergaenzt. Jede Schema-Aenderung erfordert eine Migration
 * und eine Erhoehung der version.
 *
 * Version 1: Leeres Schema (Sprint 1.3 -- Grundgeruest)
 */
@Database(
    entities = [],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TierappTypeConverters::class)
abstract class TierappDatabase : RoomDatabase()
