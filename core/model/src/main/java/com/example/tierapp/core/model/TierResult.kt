// core/model/src/main/java/com/example/tierapp/core/model/TierResult.kt
package com.example.tierapp.core.model

/**
 * Universeller Result-Wrapper für alle Repository-Operationen.
 * Kapselt Erfolg oder Fehler ohne unbehandelte Exceptions zu propagieren.
 */
sealed interface TierResult<out T> {
    data class Success<T>(val data: T) : TierResult<T>
    data class Error(
        val exception: Throwable,
        val message: String? = exception.message,
    ) : TierResult<Nothing>
}

inline fun <T> TierResult<T>.onSuccess(action: (T) -> Unit): TierResult<T> {
    if (this is TierResult.Success) action(data)
    return this
}

inline fun <T> TierResult<T>.onError(action: (TierResult.Error) -> Unit): TierResult<T> {
    if (this is TierResult.Error) action(this)
    return this
}
