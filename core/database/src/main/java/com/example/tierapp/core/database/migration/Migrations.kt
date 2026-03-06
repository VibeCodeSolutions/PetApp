package com.example.tierapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pet` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `birthDate` INTEGER,
                `species` TEXT NOT NULL,
                `breed` TEXT,
                `chipNumber` TEXT,
                `color` TEXT,
                `weightKg` REAL,
                `notes` TEXT,
                `profilePhotoId` TEXT,
                `familyId` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `syncStatus` TEXT NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
