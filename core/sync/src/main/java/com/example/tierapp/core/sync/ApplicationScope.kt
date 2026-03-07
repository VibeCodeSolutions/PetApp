package com.example.tierapp.core.sync

import javax.inject.Qualifier

/**
 * Qualifier für den prozess-weiten CoroutineScope.
 * Wird in SyncModule bereitgestellt und in RealtimeSyncObserver injiziert.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
