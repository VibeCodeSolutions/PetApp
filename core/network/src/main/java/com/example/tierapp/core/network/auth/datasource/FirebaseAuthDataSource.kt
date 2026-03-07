// core/network/src/main/java/com/example/tierapp/core/network/auth/datasource/FirebaseAuthDataSource.kt
package com.example.tierapp.core.network.auth.datasource

import com.example.tierapp.core.model.AuthUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthDataSource {

    override fun authStateChanges(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun currentUser(): AuthUser? = firebaseAuth.currentUser?.toAuthUser()

    override suspend fun signInWithCredential(credential: AuthCredential): AuthUser {
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user?.toAuthUser() ?: error("Sign-in erfolgreich, aber User ist null")
    }

    override suspend fun signOut() = firebaseAuth.signOut()

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}
