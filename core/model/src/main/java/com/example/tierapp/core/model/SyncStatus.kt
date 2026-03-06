package com.example.tierapp.core.model

enum class SyncStatus {
    /** Nur lokal vorhanden, noch nicht zu Firestore hochgeladen. */
    PENDING,
    /** Aktuell in Bearbeitung (Upload/Download laeuft). */
    IN_PROGRESS,
    /** Erfolgreich mit Firestore synchronisiert. */
    SYNCED,
    /** Letzter Sync-Versuch fehlgeschlagen. */
    FAILED,
}
