package com.example.tierapp.feature.family

import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.MemberRole
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.feature.family.fake.FakeFamilyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeFamilyRepository
    private lateinit var viewModel: FamilyViewModel

    private val testUser = AuthUser(
        uid = "user-123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    private fun makeFamily(id: String = "fam-1") = Family(
        id = id,
        name = "Meine Familie",
        createdBy = testUser.uid,
        inviteCode = "ABCD1234",
        createdAt = Instant.ofEpochMilli(1_000_000),
        updatedAt = Instant.ofEpochMilli(1_000_000),
    )

    private fun makeMember(familyId: String = "fam-1") = FamilyMember(
        id = "mem-1",
        familyId = familyId,
        userId = testUser.uid,
        displayName = testUser.displayName ?: "",
        email = testUser.email ?: "",
        role = MemberRole.OWNER,
        joinedAt = Instant.ofEpochMilli(1_000_000),
    )

    @Before
    fun setup() {
        repository = FakeFamilyRepository()
        viewModel = FamilyViewModel(repository)
    }

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is FamilyUiState.Loading)
    }

    @Test
    fun `when no family exists, state becomes NoFamily`() = runTest {
        repository.setCurrentFamily(null)
        advanceUntilIdle()
        assertEquals(FamilyUiState.NoFamily, viewModel.uiState.value)
    }

    @Test
    fun `when family exists, state becomes HasFamily with members`() = runTest {
        val family = makeFamily()
        val members = listOf(makeMember())
        repository.setCurrentFamily(family)
        repository.setMembers(members)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FamilyUiState.HasFamily)
        state as FamilyUiState.HasFamily
        assertEquals(family, state.family)
        assertEquals(members, state.members)
    }

    @Test
    fun `createFamily success updates state to HasFamily`() = runTest {
        val family = makeFamily()
        repository.createFamilyResult = TierResult.Success(family)

        viewModel.createFamily(name = "Meine Familie", owner = testUser)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FamilyUiState.HasFamily)
        assertEquals(family, (state as FamilyUiState.HasFamily).family)
    }

    @Test
    fun `createFamily error updates state to Error`() = runTest {
        repository.createFamilyResult = TierResult.Error(Exception("Netzwerkfehler"))

        viewModel.createFamily(name = "Meine Familie", owner = testUser)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is FamilyUiState.Error)
    }

    @Test
    fun `joinByInviteCode success updates state to HasFamily`() = runTest {
        val family = makeFamily()
        repository.joinByInviteCodeResult = TierResult.Success(family)

        viewModel.joinByInviteCode(inviteCode = "ABCD1234", user = testUser)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is FamilyUiState.HasFamily)
    }

    @Test
    fun `joinByInviteCode invalid code updates state to Error`() = runTest {
        repository.joinByInviteCodeResult =
            TierResult.Error(Exception("Ungültiger Einladungscode"))

        viewModel.joinByInviteCode(inviteCode = "INVALID", user = testUser)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FamilyUiState.Error)
        assertTrue((state as FamilyUiState.Error).message.contains("Einladungscode"))
    }
}
