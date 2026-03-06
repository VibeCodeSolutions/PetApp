package com.example.tierapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pet_photo` (
                `id` TEXT NOT NULL,
                `petId` TEXT NOT NULL,
                `originalPath` TEXT NOT NULL,
                `thumbSmallPath` TEXT,
                `thumbMediumPath` TEXT,
                `uploadStatus` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `syncStatus` TEXT NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`petId`) REFERENCES `pet`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pet_photo_petId` ON `pet_photo` (`petId`)")
    }
}

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
