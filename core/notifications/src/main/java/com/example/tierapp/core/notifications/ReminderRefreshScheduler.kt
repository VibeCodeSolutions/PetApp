package com.example.tierapp.core.notifications

/**
 * Abstraktion zum Planen des [ReminderRefreshWorker].
 * Ermöglicht Worker-unabhängige Unit-Tests im :core:sync-Modul.
 */
interface ReminderRefreshScheduler {
    fun scheduleOneTimeRefresh()
}
