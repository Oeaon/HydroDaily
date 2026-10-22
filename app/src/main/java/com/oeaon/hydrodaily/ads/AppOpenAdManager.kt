package com.oeaon.hydrodaily.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    private val application: Application,
    private val adUnitIds: AdUnitIds,
    private val consentManager: ConsentManager,
    private val initializer: MobileAdsInitializer,
    private val tracker: FullscreenAdTracker,
    private val adsPolicy: AdsPolicy
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var loadedAt = 0L
    private var isLoading = false
    private var currentActivity: Activity? = null
    private var lastShownAt = 0L

    fun register() {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun preload() {
        if (!canLoad() || isLoading || isAdAvailable()) return
        isLoading = true
        AppOpenAd.load(
            application,
            adUnitIds.appOpen,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadedAt = SystemClock.elapsedRealtime()
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                }
            }
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let(::showIfEligible)
    }

    private fun showIfEligible(activity: Activity) {
        if (!adsPolicy.adsEnabled.value || tracker.isShowing || !isCooldownOver()) return
        val ad = appOpenAd?.takeIf { isAdAvailable() } ?: return preload()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                tracker.onShown()
                lastShownAt = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                tracker.onDismissed()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                tracker.onDismissed()
                preload()
            }
        }
        ad.show(activity)
    }

    private fun canLoad(): Boolean =
        adsPolicy.adsEnabled.value &&
            adUnitIds.appOpen.isNotBlank() &&
            consentManager.canRequestAds &&
            initializer.isReady.value

    private fun isAdAvailable(): Boolean =
        appOpenAd != null && SystemClock.elapsedRealtime() - loadedAt < AD_EXPIRATION_MS

    private fun isCooldownOver(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val sinceLastAppOpen = if (lastShownAt == 0L) Long.MAX_VALUE else now - lastShownAt
        return sinceLastAppOpen >= MIN_INTERVAL_MS && tracker.elapsedSinceLastAdMs() >= MIN_GAP_AFTER_OTHER_AD_MS
    }

    override fun onActivityStarted(activity: Activity) {
        if (!tracker.isShowing) currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        if (!tracker.isShowing) currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private companion object {
        const val AD_EXPIRATION_MS = 4 * 60 * 60 * 1000L
        const val MIN_INTERVAL_MS = 60 * 60 * 1000L
        const val MIN_GAP_AFTER_OTHER_AD_MS = 60_000L
    }
}
