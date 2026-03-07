package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.MemberRole
import java.time.Instant

@Entity(
    tableName = "family_member",
    foreignKeys = [
        ForeignKey(
            entity = FamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("familyId"),
        Index("userId", unique = true),
    ],
)
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val userId: String,
    val displayName: String,
    val email: String,
    val role: MemberRole,
    val joinedAt: Instant,
)

fun FamilyMemberEntity.toDomain(): FamilyMember = FamilyMember(
    id = id,
    familyId = familyId,
    userId = userId,
    displayName = displayName,
    email = email,
    role = role,
    joinedAt = joinedAt,
)

fun FamilyMember.toEntity(): FamilyMemberEntity = FamilyMemberEntity(
    id = id,
    familyId = familyId,
    userId = userId,
    displayName = displayName,
    email = email,
    role = role,
    joinedAt = joinedAt,
)
