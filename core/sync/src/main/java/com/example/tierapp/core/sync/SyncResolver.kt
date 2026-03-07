package com.example.tierapp.core.sync

import com.example.tierapp.core.model.SyncStatus
import javax.inject.Inject

data class SyncMeta(
    val updatedAtMillis: Long,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
)

sealed interface SyncDecision {
    data object UseLocal : SyncDecision
    data object UseRemote : SyncDecision
    data object Skip : SyncDecision
    data object DeleteLocal : SyncDecision
    data object DeleteRemote : SyncDecision
}

class SyncResolver @Inject constructor() {

    fun resolve(local: SyncMeta?, remote: SyncMeta?): SyncDecision = when {
        local == null && remote == null -> SyncDecision.Skip
        local == null -> SyncDecision.UseRemote
        remote == null -> SyncDecision.UseLocal

        // Both deleted -> nothing to do
        local.isDeleted && remote.isDeleted -> SyncDecision.Skip
        // Delete wins regardless of timestamp
        remote.isDeleted -> SyncDecision.DeleteLocal
        local.isDeleted -> SyncDecision.DeleteRemote

        // Local already synced -> remote changes win (if actually changed)
        local.syncStatus == SyncStatus.SYNCED -> {
            if (remote.updatedAtMillis > local.updatedAtMillis) SyncDecision.UseRemote
            else SyncDecision.Skip
        }

        // Local is PENDING/FAILED/IN_PROGRESS -> Last-Write-Wins, local wins ties
        else -> {
            if (remote.updatedAtMillis > local.updatedAtMillis) SyncDecision.UseRemote
            else SyncDecision.UseLocal
        }
    }
}
