package com.example.tierapp.core.sync

import com.example.tierapp.core.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncResolverTest {

    private val resolver = SyncResolver()

    @Test
    fun `remote only - no local entity - returns UseRemote`() {
        val remote = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseRemote, resolver.resolve(local = null, remote = remote))
    }

    @Test
    fun `local only - no remote entity - returns UseLocal`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.PENDING, isDeleted = false)
        assertEquals(SyncDecision.UseLocal, resolver.resolve(local = local, remote = null))
    }

    @Test
    fun `both null - returns Skip`() {
        assertEquals(SyncDecision.Skip, resolver.resolve(local = null, remote = null))
    }

    @Test
    fun `local SYNCED and remote changed - returns UseRemote`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseRemote, resolver.resolve(local, remote))
    }

    @Test
    fun `local SYNCED and remote same timestamp - returns Skip`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.Skip, resolver.resolve(local, remote))
    }

    @Test
    fun `local PENDING and remote older - returns UseLocal`() {
        val local = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.PENDING, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseLocal, resolver.resolve(local, remote))
    }

    @Test
    fun `local PENDING and remote newer - returns UseRemote (LWW)`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.PENDING, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseRemote, resolver.resolve(local, remote))
    }

    @Test
    fun `local PENDING same timestamp as remote - returns UseLocal (local wins tie)`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.PENDING, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseLocal, resolver.resolve(local, remote))
    }

    @Test
    fun `remote deleted and local not - returns DeleteLocal`() {
        val local = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.SYNCED, isDeleted = true)
        assertEquals(SyncDecision.DeleteLocal, resolver.resolve(local, remote))
    }

    @Test
    fun `local deleted and remote not - returns DeleteRemote`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.PENDING, isDeleted = true)
        val remote = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.DeleteRemote, resolver.resolve(local, remote))
    }

    @Test
    fun `both deleted - returns Skip`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.PENDING, isDeleted = true)
        val remote = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = true)
        assertEquals(SyncDecision.Skip, resolver.resolve(local, remote))
    }

    @Test
    fun `local FAILED treated same as PENDING - remote newer wins`() {
        val local = SyncMeta(updatedAtMillis = 1000L, syncStatus = SyncStatus.FAILED, isDeleted = false)
        val remote = SyncMeta(updatedAtMillis = 2000L, syncStatus = SyncStatus.SYNCED, isDeleted = false)
        assertEquals(SyncDecision.UseRemote, resolver.resolve(local, remote))
    }
}
