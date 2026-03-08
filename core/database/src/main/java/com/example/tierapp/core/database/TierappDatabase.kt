package com.example.tierapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.dao.MedicalRecordDao
import com.example.tierapp.core.database.dao.MedicationDao
import com.example.tierapp.core.database.dao.PetDao
import com.example.tierapp.core.database.dao.PetPhotoDao
import com.example.tierapp.core.database.dao.ReminderDao
import com.example.tierapp.core.database.dao.VaccinationDao
import com.example.tierapp.core.database.entity.FamilyEntity
import com.example.tierapp.core.database.entity.FamilyMemberEntity
import com.example.tierapp.core.database.entity.MedicalRecordEntity
import com.example.tierapp.core.database.entity.MedicationEntity
import com.example.tierapp.core.database.entity.PetEntity
import com.example.tierapp.core.database.entity.PetPhotoEntity
import com.example.tierapp.core.database.entity.ReminderEntity
import com.example.tierapp.core.database.entity.VaccinationEntity

/**
 * Zentrale Room-Datenbank der Tierapp.
 *
 * Version 1: Leeres Schema (Sprint 1.3)
 * Version 2: Pet-Tabelle (Sprint 2.1)
 * Version 3: PetPhoto-Tabelle (Sprint 2.4)
 * Version 4: PetPhoto um updatedAt + Remote-URLs erweitert (Sprint 5.3)
 * Version 5: Family + FamilyMember Tabellen (Sprint 5.2)
 * Version 6: Vaccination, MedicalRecord, Medication, Reminder (Sprint 3.3-Nacharbeit)
 */
@Database(
    entities = [
        PetEntity::class,
        PetPhotoEntity::class,
        FamilyEntity::class,
        FamilyMemberEntity::class,
        VaccinationEntity::class,
        MedicalRecordEntity::class,
        MedicationEntity::class,
        ReminderEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(TierappTypeConverters::class)
abstract class TierappDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun petPhotoDao(): PetPhotoDao
    abstract fun familyDao(): FamilyDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderDao(): ReminderDao
}
