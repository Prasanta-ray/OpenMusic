package com.openmusic.app.util

import android.app.ActivityManager
import android.content.Context

object MemoryManager {

    data class MemorySnapshot(
        val totalMemMb: Long,
        val availableMemMb: Long,
        val isLowMemory: Boolean,
        val thresholdMb: Long
    )

    fun getMemorySnapshot(context: Context): MemorySnapshot {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val bytesToMb = 1024L * 1024L
        return MemorySnapshot(
            totalMemMb = memoryInfo.totalMem / bytesToMb,
            availableMemMb = memoryInfo.availMem / bytesToMb,
            isLowMemory = memoryInfo.lowMemory,
            thresholdMb = memoryInfo.threshold / bytesToMb
        )
    }

    fun isLowRamDevice(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.isLowRamDevice
    }
}
