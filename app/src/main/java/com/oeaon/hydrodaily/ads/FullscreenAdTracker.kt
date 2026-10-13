package com.oeaon.hydrodaily.ads

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FullscreenAdTracker @Inject constructor() {

    @Volatile
    var isShowing: Boolean = false
        private set

    @Volatile
    private var lastDismissedAt: Long = 0L

    fun onShown() {
        isShowing = true
    }

    fun onDismissed() {
        isShowing = false
        lastDismissedAt = SystemClock.elapsedRealtime()
    }

    fun elapsedSinceLastAdMs(): Long =
        if (lastDismissedAt == 0L) Long.MAX_VALUE else SystemClock.elapsedRealtime() - lastDismissedAt
}
