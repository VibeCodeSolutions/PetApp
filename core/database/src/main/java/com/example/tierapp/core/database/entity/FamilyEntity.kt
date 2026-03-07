package com.example.tierapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tierapp.core.model.Family
import java.time.Instant

@Entity(tableName = "family")
data class FamilyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdBy: String,
    val inviteCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun FamilyEntity.toDomain(): Family = Family(
    id = id,
    name = name,
    createdBy = createdBy,
    inviteCode = inviteCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Family.toEntity(): FamilyEntity = FamilyEntity(
    id = id,
    name = name,
    createdBy = createdBy,
    inviteCode = inviteCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
