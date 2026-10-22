package com.oeaon.hydrodaily.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitIds: AdUnitIds,
    private val consentManager: ConsentManager,
    private val initializer: MobileAdsInitializer,
    private val tracker: FullscreenAdTracker,
    private val adsPolicy: AdsPolicy
) {

    private var interstitialAd: InterstitialAd? = null
    private var loadedAt = 0L
    private var isLoading = false

    fun preload() {
        if (!canLoad() || isLoading || isAdAvailable()) return
        isLoading = true
        InterstitialAd.load(
            context,
            adUnitIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    loadedAt = SystemClock.elapsedRealtime()
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showIfReady(activity: Activity, onFinished: () -> Unit = {}) {
        val ad = interstitialAd?.takeIf { isAdAvailable() }
        if (!adsPolicy.adsEnabled.value) {
            onFinished()
            return
        }
        if (ad == null || tracker.isShowing || tracker.elapsedSinceLastAdMs() < MIN_INTERVAL_MS) {
            preload()
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                tracker.onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                tracker.onDismissed()
                preload()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                tracker.onDismissed()
                preload()
                onFinished()
            }
        }
        ad.show(activity)
    }

    private fun canLoad(): Boolean =
        adsPolicy.adsEnabled.value &&
            adUnitIds.interstitial.isNotBlank() &&
            consentManager.canRequestAds &&
            initializer.isReady.value

    private fun isAdAvailable(): Boolean =
        interstitialAd != null && SystemClock.elapsedRealtime() - loadedAt < AD_EXPIRATION_MS

    private companion object {
        const val AD_EXPIRATION_MS = 60 * 60 * 1000L
        const val MIN_INTERVAL_MS = 120_000L
    }
}
