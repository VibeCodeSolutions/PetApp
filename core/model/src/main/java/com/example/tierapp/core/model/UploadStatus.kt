package com.example.tierapp.core.model

enum class UploadStatus {
    /** Datei existiert nur lokal. */
    LOCAL_ONLY,
    /** Upload zu Firebase Storage laeuft. */
    UPLOADING,
    /** Erfolgreich hochgeladen. */
    UPLOADED,
    /** Upload fehlgeschlagen (Retry moeglich). */
    FAILED,
}
