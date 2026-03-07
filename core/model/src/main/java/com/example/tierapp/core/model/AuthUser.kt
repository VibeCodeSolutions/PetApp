// core/model/src/main/java/com/example/tierapp/core/model/AuthUser.kt
package com.example.tierapp.core.model

/**
 * Domain-Repräsentation eines eingeloggten Nutzers.
 * Entkoppelt von Firebase-spezifischen Typen.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
