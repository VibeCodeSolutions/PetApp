package com.example.tierapp.core.sync.fake

import com.example.tierapp.core.sync.SyncPreferences

class FakeSyncPreferences : SyncPreferences {
    override var lastSyncTimestamp: Long = 0L
}
