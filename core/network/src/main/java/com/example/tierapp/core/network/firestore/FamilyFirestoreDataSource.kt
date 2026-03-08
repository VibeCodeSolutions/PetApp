package com.example.tierapp.core.network.firestore

import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.MemberRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface FamilyFirestoreDataSource {
    /** Schreibt Familie-Dokument + erstes Mitglied (Owner) in Firestore. */
    suspend fun pushFamily(family: Family, owner: FamilyMember)

    /** Sucht eine Familie anhand des Einladungscodes. Null wenn nicht gefunden. */
    suspend fun getFamilyByInviteCode(inviteCode: String): Family?

    /** Fügt ein neues Mitglied zur Familie in Firestore hinzu. */
    suspend fun addMember(familyId: String, member: FamilyMember)
}

@Singleton
internal class FamilyFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : FamilyFirestoreDataSource {

    override suspend fun pushFamily(family: Family, owner: FamilyMember) {
        val batch = firestore.batch()

        val familyRef = firestore.collection("families").document(family.id)
        batch.set(familyRef, family.toFirestoreMap())

        val memberRef = familyRef.collection("members").document(owner.id)
        batch.set(memberRef, owner.toFirestoreMap())

        batch.commit().await()
    }

    override suspend fun getFamilyByInviteCode(inviteCode: String): Family? {
        val snapshot = firestore.collection("families")
            .whereEqualTo("inviteCode", inviteCode)
            .limit(1)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.data?.toFamily(doc.id)
    }

    override suspend fun addMember(familyId: String, member: FamilyMember) {
        firestore.collection("families").document(familyId)
            .collection("members").document(member.id)
            .set(member.toFirestoreMap())
            .await()
    }
}

private fun Family.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "createdBy" to createdBy,
    "inviteCode" to inviteCode,
    "createdAt" to createdAt.toEpochMilli(),
    "updatedAt" to updatedAt.toEpochMilli(),
)

private fun FamilyMember.toFirestoreMap(): Map<String, Any?> = mapOf(
    "familyId" to familyId,
    "userId" to userId,
    "displayName" to displayName,
    "email" to email,
    "role" to role.name,
    "joinedAt" to joinedAt.toEpochMilli(),
)

private fun Map<String, Any?>.toFamily(id: String): Family? {
    val name = this["name"] as? String ?: return null
    val createdBy = this["createdBy"] as? String ?: return null
    val inviteCode = this["inviteCode"] as? String ?: return null
    return Family(
        id = id,
        name = name,
        createdBy = createdBy,
        inviteCode = inviteCode,
        createdAt = Instant.ofEpochMilli(this["createdAt"] as? Long ?: 0L),
        updatedAt = Instant.ofEpochMilli(this["updatedAt"] as? Long ?: 0L),
    )
}

private fun Map<String, Any?>.toFamilyMember(id: String): FamilyMember? {
    val familyId = this["familyId"] as? String ?: return null
    val userId = this["userId"] as? String ?: return null
    return FamilyMember(
        id = id,
        familyId = familyId,
        userId = userId,
        displayName = this["displayName"] as? String ?: "",
        email = this["email"] as? String ?: "",
        role = (this["role"] as? String)?.let { runCatching { MemberRole.valueOf(it) }.getOrNull() }
            ?: MemberRole.MEMBER,
        joinedAt = Instant.ofEpochMilli(this["joinedAt"] as? Long ?: 0L),
    )
}
