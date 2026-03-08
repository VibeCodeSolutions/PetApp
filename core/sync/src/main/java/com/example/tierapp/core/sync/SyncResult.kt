package com.example.tierapp.core.sync

sealed class SyncResult {
    data object Success : SyncResult()
    data class TransientError(val cause: Exception) : SyncResult()
    data class PermanentError(val cause: Exception) : SyncResult()
}
