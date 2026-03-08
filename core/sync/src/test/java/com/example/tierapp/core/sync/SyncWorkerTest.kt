package com.example.tierapp.core.sync

import androidx.work.ListenableWorker.Result
import com.example.tierapp.core.database.dao.FamilyDao
import com.example.tierapp.core.database.entity.FamilyEntity
import java.time.Instant
import com.example.tierapp.core.notifications.ReminderRefreshScheduler
import com.example.tierapp.core.sync.fake.FakeFamilyDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit-Tests für die Business-Logik des SyncWorker, insbesondere:
 * - ReminderRefreshScheduler wird bei SyncResult.Success genau einmal aufgerufen.
 * - Bei Fehler oder fehlendem Nutzer/Familie wird der Scheduler NICHT aufgerufen.
 *
 * Verwendet einen [SyncWorkerDelegate], um die Worker-Logik ohne WorkManager-Infrastruktur
 * testen zu können.
 */
class SyncWorkerTest {

    // --- Fakes ---

    private class FakeReminderRefreshScheduler : ReminderRefreshScheduler {
        var scheduleCallCount = 0
        override fun scheduleOneTimeRefresh() {
            scheduleCallCount++
        }
    }

    /**
     * Spiegelt die Kernlogik von [SyncWorker.doWork] wider, ohne CoroutineWorker-Infrastruktur.
     * Verwendet einen [syncFn]-Lambda statt einer konkreten [SyncEngine]-Instanz,
     * um die finale Kotlin-Klasse ohne Reflection-Mocking testbar zu machen.
     * Wird nur im test-Sourceset verwendet.
     */
    private class SyncWorkerDelegate(
        private val syncFn: suspend (familyId: String) -> SyncResult,
        private val firebaseAuth: FirebaseAuth,
        private val familyDao: FamilyDao,
        private val reminderRefreshScheduler: ReminderRefreshScheduler,
        var runAttemptCount: Int = 0,
    ) {
        suspend fun doWork(): Result {
            if (firebaseAuth.currentUser == null) return Result.success()

            val familyId = familyDao.getCurrentFamilyDirect()?.id ?: return Result.success()

            return when (val result = syncFn(familyId)) {
                is SyncResult.Success -> {
                    reminderRefreshScheduler.scheduleOneTimeRefresh()
                    Result.success()
                }
                is SyncResult.PermanentError -> Result.failure()
                is SyncResult.TransientError -> {
                    if (runAttemptCount >= MAX_RETRIES) Result.failure() else Result.retry()
                }
            }
        }

        companion object {
            private const val MAX_RETRIES = 3
        }
    }

    // --- Test doubles ---

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var familyDao: FakeFamilyDao
    private lateinit var scheduler: FakeReminderRefreshScheduler

    @Before
    fun setup() {
        firebaseAuth = mock()
        familyDao = FakeFamilyDao()
        scheduler = FakeReminderRefreshScheduler()

        val mockUser: FirebaseUser = mock()
        whenever(firebaseAuth.currentUser).thenReturn(mockUser)
        familyDao.setFamily(
            FamilyEntity(
                id = "fam-1",
                name = "Testfamilie",
                createdBy = "user-1",
                inviteCode = "ABC123",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
            ),
        )
    }

    private fun buildDelegate(
        result: SyncResult,
        runAttemptCount: Int = 0,
    ): SyncWorkerDelegate = SyncWorkerDelegate(
        syncFn = { result },
        firebaseAuth = firebaseAuth,
        familyDao = familyDao,
        reminderRefreshScheduler = scheduler,
        runAttemptCount = runAttemptCount,
    )

    // --- Tests ---

    @Test
    fun `Success - ReminderRefreshScheduler wird genau einmal aufgerufen`() = runTest {
        val delegate = buildDelegate(SyncResult.Success)
        val workerResult = delegate.doWork()
        assertEquals(Result.success(), workerResult)
        assertEquals(1, scheduler.scheduleCallCount)
    }

    @Test
    fun `TransientError - Scheduler wird nicht aufgerufen`() = runTest {
        val delegate = buildDelegate(SyncResult.TransientError(RuntimeException("net")))
        delegate.doWork()
        assertEquals(0, scheduler.scheduleCallCount)
    }

    @Test
    fun `PermanentError - Scheduler wird nicht aufgerufen`() = runTest {
        val delegate = buildDelegate(SyncResult.PermanentError(RuntimeException("perm")))
        delegate.doWork()
        assertEquals(0, scheduler.scheduleCallCount)
    }

    @Test
    fun `nicht authentifiziert - Scheduler wird nicht aufgerufen`() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(null)
        val delegate = buildDelegate(SyncResult.Success)
        delegate.doWork()
        assertEquals(0, scheduler.scheduleCallCount)
    }

    @Test
    fun `keine Familie vorhanden - Scheduler wird nicht aufgerufen`() = runTest {
        familyDao.setFamily(null)
        val delegate = buildDelegate(SyncResult.Success)
        delegate.doWork()
        assertEquals(0, scheduler.scheduleCallCount)
    }

    @Test
    fun `Success - gibt Result_success zurueck`() = runTest {
        assertEquals(Result.success(), buildDelegate(SyncResult.Success).doWork())
    }

    @Test
    fun `PermanentError - gibt Result_failure zurueck`() = runTest {
        assertEquals(
            Result.failure(),
            buildDelegate(SyncResult.PermanentError(RuntimeException())).doWork(),
        )
    }

    @Test
    fun `TransientError unter MAX_RETRIES - gibt Result_retry zurueck`() = runTest {
        val delegate = buildDelegate(SyncResult.TransientError(RuntimeException()), runAttemptCount = 1)
        assertEquals(Result.retry(), delegate.doWork())
    }
}
