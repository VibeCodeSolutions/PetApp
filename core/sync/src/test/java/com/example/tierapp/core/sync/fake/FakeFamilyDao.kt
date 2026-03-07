package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.entity.FamilyEntity
import com.example.tierapp.core.database.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFamilyDao(
    initialFamily: FamilyEntity? = null,
) : FamilyDao {

    private val familyFlow = MutableStateFlow<FamilyEntity?>(initialFamily)
    private val membersStore = mutableMapOf<String, FamilyMemberEntity>()

    fun setFamily(family: FamilyEntity?) {
        familyFlow.value = family
    }

    override fun observeCurrentFamily(): Flow<FamilyEntity?> = familyFlow

    override suspend fun getCurrentFamilyDirect(): FamilyEntity? = familyFlow.value

    override suspend fun insertFamily(family: FamilyEntity) {
        familyFlow.value = family
    }

    override fun observeMembers(familyId: String): Flow<List<FamilyMemberEntity>> =
        familyFlow.map { membersStore.values.filter { it.familyId == familyId } }

    override suspend fun insertMember(member: FamilyMemberEntity) {
        membersStore[member.id] = member
    }

    override suspend fun getMemberByUserId(familyId: String, userId: String): FamilyMemberEntity? =
        membersStore.values.find { it.familyId == familyId && it.userId == userId }

    override suspend fun deleteAll() {
        familyFlow.value = null
        membersStore.clear()
    }
}
