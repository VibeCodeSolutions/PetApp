// core/network/src/main/java/com/example/tierapp/core/network/auth/AuthRepository.kt
package com.example.tierapp.core.network.auth

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.TierResult
import kotlinx.coroutines.flow.Flow

/**
 * Kontrakt für alle Firebase-Auth-Operationen.
 * Firestore/Auth-Zugriffe erfolgen ausschließlich über :core:network.
 */
interface AuthRepository {
    /** Emittiert null (ausgeloggt) oder AuthUser (eingeloggt). Cold Flow. */
    val authState: Flow<AuthUser?>

    fun currentUser(): AuthUser?

    suspend fun signInWithGoogle(idToken: String): TierResult<AuthUser>

    suspend fun signOut(): TierResult<Unit>
}
