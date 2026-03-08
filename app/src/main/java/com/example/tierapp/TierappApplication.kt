// app/src/main/java/com/example/tierapp/TierappApplication.kt
package com.example.tierapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.example.tierapp.core.sync.RealtimeSyncObserver
import com.example.tierapp.core.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

@HiltAndroidApp
class TierappApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var realtimeSyncObserver: RealtimeSyncObserver

    override fun onCreate() {
        super.onCreate()
        syncScheduler.schedulePeriodicSync()
        realtimeSyncObserver.register()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // Coil 3: globaler ImageLoader mit strikten Cache-Limits
    // Memory: max 64 MB, Disk: max 250 MB — verhindert OOM bei vielen Tierfotos
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(64 * 1024 * 1024) // 64 MB
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache").toOkioPath())
                    .maxSizeBytes(250L * 1024 * 1024) // 250 MB
                    .build()
            }
            .build()
}
