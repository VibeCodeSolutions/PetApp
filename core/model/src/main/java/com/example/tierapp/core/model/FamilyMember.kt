package com.example.tierapp.core.model

import java.time.Instant

data class FamilyMember(
    val id: String,
    val familyId: String,
    val userId: String,
    val displayName: String,
    val email: String,
    val role: MemberRole,
    val joinedAt: Instant,
)
