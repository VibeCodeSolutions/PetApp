// core/network/src/main/java/com/example/tierapp/core/network/auth/datasource/AuthDataSource.kt
package com.example.tierapp.core.network.auth.datasource

import com.example.tierapp.core.model.AuthUser
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow

/**
 * Interne Abstraktion über Firebase-Auth-Aufrufe.
 * Gibt bereits gemappte Domain-Typen zurück — kein FirebaseUser außerhalb dieser Schicht.
 * Ermöglicht echte Unit-Tests von FirebaseAuthRepository ohne Firebase-Laufzeit.
 */
internal interface AuthDataSource {
    fun authStateChanges(): Flow<AuthUser?>
    fun currentUser(): AuthUser?
    suspend fun signInWithCredential(credential: AuthCredential): AuthUser
    suspend fun signOut()
}
