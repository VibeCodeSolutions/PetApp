package com.example.tierapp.feature.family

import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.MemberRole
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.firestore.FamilyFirestoreDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class FamilyRepositoryImplTest {

    private lateinit var firestoreDataSource: FamilyFirestoreDataSource
    private lateinit var familyDao: FamilyDao
    private lateinit var repository: FamilyRepositoryImpl

    private val testFamily = Family(
        id = "fam-1",
        name = "Testfamilie",
        createdBy = "user-1",
        inviteCode = "ABCD1234",
        createdAt = Instant.ofEpochMilli(1_000_000),
        updatedAt = Instant.ofEpochMilli(1_000_000),
    )

    private val testUser = AuthUser(
        uid = "user-2",
        email = "b@example.com",
        displayName = "User B",
        photoUrl = null,
    )

    private val existingMember = FamilyMember(
        id = "mem-1",
        familyId = "fam-1",
        userId = "user-1",
        displayName = "User A",
        email = "a@example.com",
        role = MemberRole.OWNER,
        joinedAt = Instant.ofEpochMilli(1_000_000),
    )

    @Before
    fun setup() {
        firestoreDataSource = mock()
        familyDao = mock()
        repository = FamilyRepositoryImpl(familyDao, firestoreDataSource)
    }

    /**
     * Kerntest: Verifiziert die korrekte Aufruf-Reihenfolge im Join-Flow.
     *
     * Erwartet: addMember() → fetchMembers() → insertFamily() (Room)
     *
     * Hintergrund: addMember() muss vor fetchMembers() stehen, damit
     * isFamilyMember(familyId) in den Firestore Security Rules true ergibt.
     * Andernfalls schlägt der Read mit "Permission Denied" fehl.
     */
    @Test
    fun `joinByInviteCode ruft addMember vor fetchMembers auf (InOrder)`() = runTest {
        whenever(firestoreDataSource.getFamilyByInviteCode("ABCD1234"))
            .thenReturn(testFamily)
        whenever(firestoreDataSource.fetchMembers("fam-1"))
            .thenReturn(listOf(existingMember))

        val result = repository.joinByInviteCode("ABCD1234", testUser)

        assertTrue("Erwarte TierResult.Success", result is TierResult.Success)

        // InOrder-Verification: addMember() muss VOR fetchMembers() stehen,
        // dann erst darf Room beschrieben werden.
        val order = inOrder(firestoreDataSource, familyDao)
        order.verify(firestoreDataSource).addMember(any(), any())
        order.verify(firestoreDataSource).fetchMembers("fam-1")
        order.verify(familyDao).insertFamily(any())
    }

    @Test
    fun `joinByInviteCode mit ungueltigem Code gibt Error zurueck`() = runTest {
        whenever(firestoreDataSource.getFamilyByInviteCode(any()))
            .thenReturn(null)

        val result = repository.joinByInviteCode("INVALID0", testUser)

        assertTrue("Erwarte TierResult.Error bei ungültigem Code", result is TierResult.Error)
    }

    @Test
    fun `joinByInviteCode normalisiert Code (trim + uppercase) vor Firestore-Abfrage`() = runTest {
        whenever(firestoreDataSource.getFamilyByInviteCode("ABCD1234"))
            .thenReturn(testFamily)
        whenever(firestoreDataSource.fetchMembers("fam-1"))
            .thenReturn(emptyList())

        // Code mit Leerzeichen und Kleinbuchstaben
        val result = repository.joinByInviteCode("  abcd1234  ", testUser)

        assertTrue("Normalisierter Code muss zur Familie führen", result is TierResult.Success)
    }
}
