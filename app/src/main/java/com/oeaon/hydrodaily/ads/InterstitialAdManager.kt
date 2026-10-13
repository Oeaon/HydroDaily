package com.oeaon.hydrodaily.ads

import android.app.Activity
import android.content.Context
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
    private val tracker: FullscreenAdTracker
) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload() {
        if (!canLoad() || isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            context,
            adUnitIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
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
        val ad = interstitialAd
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
        adUnitIds.interstitial.isNotBlank() && consentManager.canRequestAds && initializer.isReady.value

    private companion object {
        const val MIN_INTERVAL_MS = 120_000L
    }
}
