package com.oeaon.hydrodaily.ads

import com.oeaon.hydrodaily.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdUnitIds @Inject constructor() {
    val banner: String = BuildConfig.AD_BANNER_ID
    val interstitial: String = BuildConfig.AD_INTERSTITIAL_ID
    val appOpen: String = BuildConfig.AD_APP_OPEN_ID
}
