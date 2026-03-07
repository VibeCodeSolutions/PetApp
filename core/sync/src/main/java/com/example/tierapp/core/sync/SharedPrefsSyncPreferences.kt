package com.example.tierapp.core.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsSyncPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : SyncPreferences {

    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    override var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) { prefs.edit().putLong(KEY_LAST_SYNC, value).apply() }

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}
