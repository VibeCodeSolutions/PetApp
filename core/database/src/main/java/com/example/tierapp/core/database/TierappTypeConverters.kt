package com.example.tierapp.core.database

import androidx.room.TypeConverter
import com.example.tierapp.core.model.MedicalRecordType
import com.example.tierapp.core.model.MemberRole
import com.example.tierapp.core.model.PetSpecies
import com.example.tierapp.core.model.ReminderType
import com.example.tierapp.core.model.SyncStatus
import com.example.tierapp.core.model.UploadStatus
import java.time.Instant
import java.time.LocalDate

class TierappTypeConverters {

    // LocalDate <-> Long (EpochDay)
    @TypeConverter
    fun localDateToLong(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun longToLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    // Instant <-> Long (EpochMilli)
    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    // Enums <-> String (name-basiert; stabil solange Enum-Namen unveraendert bleiben)
    @TypeConverter
    fun petSpeciesToString(value: PetSpecies?): String? = value?.name

    @TypeConverter
    fun stringToPetSpecies(value: String?): PetSpecies? = value?.let { PetSpecies.valueOf(it) }

    @TypeConverter
    fun syncStatusToString(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun stringToSyncStatus(value: String?): SyncStatus? = value?.let { SyncStatus.valueOf(it) }

    @TypeConverter
    fun medicalRecordTypeToString(value: MedicalRecordType?): String? = value?.name

    @TypeConverter
    fun stringToMedicalRecordType(value: String?): MedicalRecordType? =
        value?.let { MedicalRecordType.valueOf(it) }

    @TypeConverter
    fun memberRoleToString(value: MemberRole?): String? = value?.name

    @TypeConverter
    fun stringToMemberRole(value: String?): MemberRole? = value?.let { MemberRole.valueOf(it) }

    @TypeConverter
    fun uploadStatusToString(value: UploadStatus?): String? = value?.name

    @TypeConverter
    fun stringToUploadStatus(value: String?): UploadStatus? =
        value?.let { UploadStatus.valueOf(it) }

    @TypeConverter
    fun reminderTypeToString(value: ReminderType?): String? = value?.name

    @TypeConverter
    fun stringToReminderType(value: String?): ReminderType? =
        value?.let { ReminderType.valueOf(it) }
}
