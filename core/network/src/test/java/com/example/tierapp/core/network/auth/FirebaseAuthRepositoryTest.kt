// core/network/src/test/java/com/example/tierapp/core/network/auth/FirebaseAuthRepositoryTest.kt
package com.example.tierapp.core.network.auth

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.auth.datasource.AuthDataSource
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthRepositoryTest {

    // ---- authState ----------------------------------------------------------

    @Test
    fun `authState emittiert null wenn kein User angemeldet`() = runTest {
        val repo = buildRepo(currentUser = null)
        assertNull(repo.authState.first())
    }

    @Test
    fun `authState emittiert AuthUser wenn User angemeldet`() = runTest {
        val user = testUser("uid-1", "test@example.com", "Max")
        val repo = buildRepo(currentUser = user)
        assertEquals(user, repo.authState.first())
    }

    @Test
    fun `authState reagiert reaktiv auf Zustandsaenderungen`() = runTest {
        val fakeDataSource = FakeAuthDataSource(currentUser = null)
        val repo = FirebaseAuthRepository(fakeDataSource)
        val collected = mutableListOf<AuthUser?>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.authState.collect { collected += it }
        }
        advanceUntilIdle()

        val newUser = testUser("uid-2", "user@test.de", "Anna")
        fakeDataSource.emitUser(newUser)
        advanceUntilIdle()

        assertEquals(2, collected.size)
        assertNull(collected[0])
        assertEquals(newUser, collected[1])
    }

    // ---- currentUser --------------------------------------------------------

    @Test
    fun `currentUser gibt null zurueck wenn nicht eingeloggt`() {
        assertNull(buildRepo(currentUser = null).currentUser())
    }

    @Test
    fun `currentUser gibt AuthUser zurueck wenn eingeloggt`() {
        val user = testUser("uid-3", "a@b.com", "Lea")
        assertEquals(user, buildRepo(currentUser = user).currentUser())
    }

    // ---- signInWithGoogle ---------------------------------------------------

    @Test
    fun `signInWithGoogle gibt Success mit AuthUser zurueck`() = runTest {
        val user = testUser("uid-g", "google@test.de", "Google User")
        val result = buildRepo(signInResult = user).signInWithGoogle("valid-id-token")
        assertTrue(result is TierResult.Success)
        assertEquals(user, (result as TierResult.Success).data)
    }

    @Test
    fun `signInWithGoogle gibt Error zurueck wenn DataSource wirft`() = runTest {
        val result = buildRepo(signInThrows = RuntimeException("Netzwerkfehler"))
            .signInWithGoogle("broken-token")
        assertTrue(result is TierResult.Error)
        assertEquals("Netzwerkfehler", (result as TierResult.Error).exception.message)
    }

    // ---- signOut ------------------------------------------------------------

    @Test
    fun `signOut gibt Success zurueck`() = runTest {
        assertTrue(buildRepo().signOut() is TierResult.Success)
    }

    @Test
    fun `signOut gibt Error zurueck wenn DataSource wirft`() = runTest {
        val result = buildRepo(signOutThrows = RuntimeException("Sign-out Fehler")).signOut()
        assertTrue(result is TierResult.Error)
    }

    // ---- Hilfsfunktionen ---------------------------------------------------

    private fun testUser(uid: String, email: String, displayName: String) =
        AuthUser(uid = uid, email = email, displayName = displayName, photoUrl = null)

    private fun buildRepo(
        currentUser: AuthUser? = null,
        signInResult: AuthUser? = currentUser,
        signInThrows: Exception? = null,
        signOutThrows: Exception? = null,
    ): FirebaseAuthRepository = FirebaseAuthRepository(
        FakeAuthDataSource(
            currentUser = currentUser,
            signInResult = signInResult,
            signInThrows = signInThrows,
            signOutThrows = signOutThrows,
        ),
    )
}

// ---- Fake ------------------------------------------------------------------

private class FakeAuthDataSource(
    currentUser: AuthUser? = null,
    private val signInResult: AuthUser? = currentUser,
    private val signInThrows: Exception? = null,
    private val signOutThrows: Exception? = null,
) : AuthDataSource {
    private val userFlow = MutableStateFlow(currentUser)

    fun emitUser(user: AuthUser?) { userFlow.value = user }

    override fun authStateChanges(): Flow<AuthUser?> = userFlow
    override fun currentUser(): AuthUser? = userFlow.value

    override suspend fun signInWithCredential(credential: AuthCredential): AuthUser {
        signInThrows?.let { throw it }
        return signInResult ?: error("Kein signInResult konfiguriert")
    }

    override suspend fun signOut() {
        signOutThrows?.let { throw it }
    }
}
