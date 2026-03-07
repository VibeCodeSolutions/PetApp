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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pet_photo ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE pet_photo SET updatedAt = createdAt")
        db.execSQL("ALTER TABLE pet_photo ADD COLUMN remoteOriginalUrl TEXT")
        db.execSQL("ALTER TABLE pet_photo ADD COLUMN remoteThumbSmallUrl TEXT")
        db.execSQL("ALTER TABLE pet_photo ADD COLUMN remoteThumbMediumUrl TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `family` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `inviteCode` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `family_member` (
                `id` TEXT NOT NULL,
                `familyId` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `role` TEXT NOT NULL,
                `joinedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`familyId`) REFERENCES `family`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_family_member_familyId` ON `family_member` (`familyId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_family_member_userId` ON `family_member` (`userId`)")
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
