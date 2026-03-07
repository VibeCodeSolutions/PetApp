package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialUser: AuthUser? = null,
) : AuthRepository {

    private val _authState = MutableStateFlow(initialUser)
    override val authState: Flow<AuthUser?> = _authState

    var currentUserValue: AuthUser? = initialUser

    override fun currentUser(): AuthUser? = currentUserValue

    override suspend fun signInWithGoogle(idToken: String): TierResult<AuthUser> =
        error("Not implemented in fake")

    override suspend fun signOut(): TierResult<Unit> =
        error("Not implemented in fake")
}
