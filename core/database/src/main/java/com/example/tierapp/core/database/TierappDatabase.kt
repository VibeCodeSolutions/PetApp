package com.example.tierapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.entity.FamilyEntity
import com.example.tierapp.core.database.entity.FamilyMemberEntity
import com.example.tierapp.core.database.entity.PetEntity
import com.example.tierapp.core.database.entity.PetPhotoEntity

/**
 * Zentrale Room-Datenbank der Tierapp.
 *
 * Version 1: Leeres Schema (Sprint 1.3)
 * Version 2: Pet-Tabelle (Sprint 2.1)
 * Version 3: PetPhoto-Tabelle (Sprint 2.4)
 * Version 4: PetPhoto um updatedAt + Remote-URLs erweitert (Sprint 5.3)
 * Version 5: Family + FamilyMember Tabellen (Sprint 5.2)
 */
@Database(
    entities = [PetEntity::class, PetPhotoEntity::class, FamilyEntity::class, FamilyMemberEntity::class],
    version = 5,
    exportSchema = true,
)
@TypeConverters(TierappTypeConverters::class)
abstract class TierappDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun petPhotoDao(): PetPhotoDao
    abstract fun familyDao(): FamilyDao
}
