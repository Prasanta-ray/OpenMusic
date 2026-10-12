package com.openmusic.app

import android.app.Application
import android.content.ComponentCallbacks2
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File

@OptIn(UnstableApi::class)
class MainApplication : Application(), ImageLoaderFactory {

    companion object {
        private const val MEDIA_CACHE_SUBDIR = "media_cache"
        private const val MAX_CACHE_SIZE_BYTES = 2L * 1024 * 1024 * 1024 // 2 GB

        @Volatile
        private var simpleCacheInstance: SimpleCache? = null

        /**
         * Returns the application-wide ExoPlayer SimpleCache singleton.
         */
        @Synchronized
        fun getMediaCache(application: Application): SimpleCache {
            return simpleCacheInstance ?: run {
                val cacheDirectory = File(application.cacheDir, MEDIA_CACHE_SUBDIR)
                val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
                val databaseProvider = StandaloneDatabaseProvider(application)
                SimpleCache(cacheDirectory, evictor, databaseProvider).also {
                    simpleCacheInstance = it
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-warm the cache instance
        getMediaCache(this)
    }

    /**
     * Optimized Coil ImageLoader for low-RAM devices (Snapdragon 675 profile).
     * Restricts memory cache to 20% of app heap and caps disk cache at 100MB.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB max for album art
                    .build()
            }
            .crossfade(true)
            .build()
    }

    /**
     * Aggressive memory management for devices with limited memory headroom.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val memoryCache = coil.Coil.imageLoader(this).memoryCache
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Clear bitmap memory cache immediately on critical memory signals
                memoryCache?.clear()
                System.gc()
            }
            else -> {
                // Pass TRIM_MEMORY_UI_HIDDEN, TRIM_MEMORY_RUNNING_LOW, etc. directly to Coil
                memoryCache?.trimMemory(level)
            }
        }
    }
}
