// core/network/src/main/java/com/example/tierapp/core/network/auth/FirebaseAuthRepository.kt
package com.example.tierapp.core.network.auth

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.auth.datasource.AuthDataSource
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FirebaseAuthRepository @Inject constructor(
    private val dataSource: AuthDataSource,
) : AuthRepository {

    override val authState: Flow<AuthUser?> = dataSource.authStateChanges()

    override fun currentUser(): AuthUser? = dataSource.currentUser()

    override suspend fun signInWithGoogle(idToken: String): TierResult<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                dataSource.signInWithCredential(credential)
            }.toTierResult()
        }

    override suspend fun signOut(): TierResult<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { dataSource.signOut() }.toTierResult()
        }
}

private fun <T> Result<T>.toTierResult(): TierResult<T> =
    fold(
        onSuccess = { TierResult.Success(it) },
        onFailure = { TierResult.Error(it) },
    )
