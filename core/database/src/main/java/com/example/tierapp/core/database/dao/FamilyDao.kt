package com.example.tierapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tierapp.core.database.entity.FamilyEntity
import com.example.tierapp.core.database.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    @Query("SELECT * FROM family LIMIT 1")
    fun observeCurrentFamily(): Flow<FamilyEntity?>

    @Query("SELECT * FROM family LIMIT 1")
    suspend fun getCurrentFamilyDirect(): FamilyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    @Query("SELECT * FROM family_member WHERE familyId = :familyId ORDER BY joinedAt ASC")
    fun observeMembers(familyId: String): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity)

    @Query("SELECT * FROM family_member WHERE familyId = :familyId AND userId = :userId LIMIT 1")
    suspend fun getMemberByUserId(familyId: String, userId: String): FamilyMemberEntity?

    @Query("DELETE FROM family")
    suspend fun deleteAll()
}
