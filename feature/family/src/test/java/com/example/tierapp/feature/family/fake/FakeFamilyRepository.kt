package com.example.tierapp.feature.family.fake

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.FamilyRepository
import com.example.tierapp.core.model.TierResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFamilyRepository : FamilyRepository {

    private val familyFlow = MutableStateFlow<Family?>(null)
    private val membersFlow = MutableStateFlow<List<FamilyMember>>(emptyList())

    var createFamilyResult: TierResult<Family>? = null
    var joinByInviteCodeResult: TierResult<Family>? = null

    fun setCurrentFamily(family: Family?) {
        familyFlow.value = family
    }

    fun setMembers(members: List<FamilyMember>) {
        membersFlow.value = members
    }

    override val currentFamilyId: Flow<String?> = familyFlow.map { it?.id }

    override fun observeCurrentFamily(): Flow<Family?> = familyFlow

    override fun observeMembers(familyId: String): Flow<List<FamilyMember>> =
        membersFlow.map { it.filter { m -> m.familyId == familyId } }

    override suspend fun createFamily(name: String, owner: AuthUser): TierResult<Family> {
        val result = createFamilyResult ?: error("createFamilyResult not set")
        if (result is TierResult.Success) familyFlow.value = result.data
        return result
    }

    override suspend fun joinByInviteCode(inviteCode: String, user: AuthUser): TierResult<Family> {
        val result = joinByInviteCodeResult ?: error("joinByInviteCodeResult not set")
        if (result is TierResult.Success) familyFlow.value = result.data
        return result
    }
}
