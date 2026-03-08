package com.example.tierapp.feature.family

import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.entity.toDomain
import com.example.tierapp.core.database.entity.toEntity
import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.FamilyRepository
import com.example.tierapp.core.model.MemberRole
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.firestore.FamilyFirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

internal class FamilyRepositoryImpl @Inject constructor(
    private val familyDao: FamilyDao,
    private val familyFirestoreDataSource: FamilyFirestoreDataSource,
) : FamilyRepository {

    override val currentFamilyId: Flow<String?> =
        familyDao.observeCurrentFamily().map { it?.id }

    override fun observeCurrentFamily(): Flow<Family?> =
        familyDao.observeCurrentFamily().map { it?.toDomain() }

    override fun observeMembers(familyId: String): Flow<List<FamilyMember>> =
        familyDao.observeMembers(familyId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createFamily(name: String, owner: AuthUser): TierResult<Family> {
        return try {
            val now = Instant.now()
            val family = Family(
                id = UUID.randomUUID().toString(),
                name = name,
                createdBy = owner.uid,
                inviteCode = generateInviteCode(),
                createdAt = now,
                updatedAt = now,
            )
            val ownerMember = FamilyMember(
                id = UUID.randomUUID().toString(),
                familyId = family.id,
                userId = owner.uid,
                displayName = owner.displayName ?: owner.email ?: owner.uid,
                email = owner.email ?: "",
                role = MemberRole.OWNER,
                joinedAt = now,
            )
            // Room zuerst (optimistic, SSOT)
            familyDao.insertFamily(family.toEntity())
            familyDao.insertMember(ownerMember.toEntity())
            // Firestore sync
            familyFirestoreDataSource.pushFamily(family, ownerMember)
            TierResult.Success(family)
        } catch (e: Exception) {
            TierResult.Error(e)
        }
    }

    override suspend fun joinByInviteCode(inviteCode: String, user: AuthUser): TierResult<Family> {
        return try {
            val normalizedCode = inviteCode.trim().uppercase()
            val remoteFamily = familyFirestoreDataSource.getFamilyByInviteCode(normalizedCode)
                ?: return TierResult.Error(Exception("Ungültiger Einladungscode"))

            val now = Instant.now()
            val member = FamilyMember(
                id = UUID.randomUUID().toString(),
                familyId = remoteFamily.id,
                userId = user.uid,
                displayName = user.displayName ?: user.email ?: user.uid,
                email = user.email ?: "",
                role = MemberRole.MEMBER,
                joinedAt = now,
            )
            // 1) Eigenes Member-Dokument ZUERST in Firestore schreiben,
            //    damit isFamilyMember(familyId) in den Security Rules greift.
            familyFirestoreDataSource.addMember(remoteFamily.id, member)
            // 2) Jetzt darf der Client die Members-Subcollection lesen.
            val allMembers = familyFirestoreDataSource.fetchMembers(remoteFamily.id)
            // 3) Alles lokal in Room speichern (SSOT)
            familyDao.insertFamily(remoteFamily.toEntity())
            allMembers.forEach { familyDao.insertMember(it.toEntity()) }
            // Eigenen Member nur inserten wenn er NICHT in allMembers enthalten ist
            // (Firestore-Propagation kann verzögert sein)
            if (allMembers.none { it.userId == member.userId }) {
                familyDao.insertMember(member.toEntity())
            }
            TierResult.Success(remoteFamily)
        } catch (e: Exception) {
            TierResult.Error(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
