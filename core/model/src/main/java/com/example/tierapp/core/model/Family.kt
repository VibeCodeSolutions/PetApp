package com.example.tierapp.core.model

import java.time.Instant

data class Family(
    val id: String,
    val name: String,
    val createdBy: String,
    val inviteCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
