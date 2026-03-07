// app/src/test/java/com/example/tierapp/auth/LoginViewModelTest.kt
package com.example.tierapp.auth

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---- Initialzustand -----------------------------------------------------

    @Test
    fun `initialer Zustand ist Unauthenticated wenn kein User`() = runTest {
        val vm = buildViewModel(currentUser = null)
        assertEquals(LoginUiState.Unauthenticated, vm.uiState.value)
    }

    @Test
    fun `initialer Zustand ist Authenticated wenn User bereits eingeloggt`() = runTest {
        val user = testUser("uid-init")
        val vm = buildViewModel(currentUser = user)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(LoginUiState.Authenticated(user), vm.uiState.value)
    }

    // ---- Auth-State-Reaktivität ---------------------------------------------

    @Test
    fun `uiState wird Authenticated wenn authState einen User emittiert`() = runTest {
        val fakeRepo = FakeAuthRepository(currentUser = null)
        val vm = LoginViewModel(fakeRepo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        val user = testUser("uid-reactive")
        fakeRepo.emitUser(user)
        advanceUntilIdle()

        assertEquals(LoginUiState.Authenticated(user), vm.uiState.value)
    }

    @Test
    fun `uiState wird Unauthenticated wenn authState null emittiert`() = runTest {
        val user = testUser("uid-out")
        val fakeRepo = FakeAuthRepository(currentUser = user)
        val vm = LoginViewModel(fakeRepo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        fakeRepo.emitUser(null)
        advanceUntilIdle()

        assertEquals(LoginUiState.Unauthenticated, vm.uiState.value)
    }

    // ---- signInWithGoogle ---------------------------------------------------

    @Test
    fun `signInWithGoogle setzt Authenticated bei Erfolg`() = runTest {
        val user = testUser("uid-g")
        val vm = buildViewModel(signInResult = TierResult.Success(user))
        collectInBackground { vm.uiState.collect {} }

        vm.signInWithGoogle("id-token")
        advanceUntilIdle()

        assertEquals(LoginUiState.Authenticated(user), vm.uiState.value)
    }

    @Test
    fun `signInWithGoogle setzt Error bei Fehler`() = runTest {
        val vm = buildViewModel(signInResult = TierResult.Error(RuntimeException("Google Fehler")))
        collectInBackground { vm.uiState.collect {} }

        vm.signInWithGoogle("broken-token")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is LoginUiState.Error)
        assertEquals("Google Fehler", (vm.uiState.value as LoginUiState.Error).message)
    }

    @Test
    fun `signInWithGoogle durchlaeuft Loading-Zustand`() = runTest {
        val states = mutableListOf<LoginUiState>()
        val user = testUser("uid-g2")
        val vm = buildViewModel(signInResult = TierResult.Success(user))
        collectInBackground { vm.uiState.collect { states += it } }
        advanceUntilIdle()

        vm.signInWithGoogle("token")
        advanceUntilIdle()

        assertTrue(states.contains(LoginUiState.Loading))
    }

    // ---- signOut ------------------------------------------------------------

    @Test
    fun `signOut setzt Unauthenticated nach Erfolg`() = runTest {
        val user = testUser("uid-so")
        val fakeRepo = FakeAuthRepository(currentUser = user, signOutResult = TierResult.Success(Unit))
        val vm = LoginViewModel(fakeRepo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.signOut()
        fakeRepo.emitUser(null) // Simulate Firebase authState update after sign-out
        advanceUntilIdle()

        assertEquals(LoginUiState.Unauthenticated, vm.uiState.value)
    }

    @Test
    fun `signOut setzt Error wenn signOut fehlschlaegt`() = runTest {
        val user = testUser("uid-so-err")
        val fakeRepo = FakeAuthRepository(
            currentUser = user,
            signOutResult = TierResult.Error(RuntimeException("Sign-out Fehler")),
        )
        val vm = LoginViewModel(fakeRepo)
        collectInBackground { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.signOut()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is LoginUiState.Error)
    }

    // ---- clearError ---------------------------------------------------------

    @Test
    fun `clearError setzt Zustand von Error auf Unauthenticated zurueck`() = runTest {
        val vm = buildViewModel(signInResult = TierResult.Error(RuntimeException("Fehler")))
        collectInBackground { vm.uiState.collect {} }
        vm.signInWithGoogle("token")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is LoginUiState.Error)

        vm.clearError()
        advanceUntilIdle()

        assertEquals(LoginUiState.Unauthenticated, vm.uiState.value)
    }

    // ---- Hilfsfunktionen ---------------------------------------------------

    private fun testUser(uid: String) =
        AuthUser(uid = uid, email = "$uid@test.de", displayName = uid, photoUrl = null)

    private fun buildViewModel(
        currentUser: AuthUser? = null,
        signInResult: TierResult<AuthUser> = TierResult.Success(currentUser ?: testUser("default")),
        signOutResult: TierResult<Unit> = TierResult.Success(Unit),
    ): LoginViewModel = LoginViewModel(
        FakeAuthRepository(currentUser = currentUser, signInResult = signInResult, signOutResult = signOutResult),
    )

    private fun kotlinx.coroutines.test.TestScope.collectInBackground(block: suspend () -> Unit) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { block() }
    }
}

// ---- Fake ------------------------------------------------------------------

private class FakeAuthRepository(
    currentUser: AuthUser? = null,
    private val signInResult: TierResult<AuthUser> = TierResult.Success(
        currentUser ?: AuthUser("uid", "e@e.com", "Test", null)
    ),
    private val signOutResult: TierResult<Unit> = TierResult.Success(Unit),
) : AuthRepository {
    private val userFlow = MutableStateFlow(currentUser)

    fun emitUser(user: AuthUser?) { userFlow.value = user }

    override val authState: Flow<AuthUser?> = userFlow
    override fun currentUser(): AuthUser? = userFlow.value

    override suspend fun signInWithGoogle(idToken: String): TierResult<AuthUser> = signInResult
    override suspend fun signOut(): TierResult<Unit> = signOutResult
}
