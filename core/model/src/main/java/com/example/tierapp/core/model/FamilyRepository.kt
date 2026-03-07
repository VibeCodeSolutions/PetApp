package com.example.tierapp.core.model

import kotlinx.coroutines.flow.Flow

interface FamilyRepository {

    /** Emittiert die familyId des aktuellen Users. Null solange keine Familie existiert. */
    val currentFamilyId: Flow<String?>

    fun observeCurrentFamily(): Flow<Family?>

    fun observeMembers(familyId: String): Flow<List<FamilyMember>>

    suspend fun createFamily(name: String, owner: AuthUser): TierResult<Family>

    suspend fun joinByInviteCode(inviteCode: String, user: AuthUser): TierResult<Family>
}
