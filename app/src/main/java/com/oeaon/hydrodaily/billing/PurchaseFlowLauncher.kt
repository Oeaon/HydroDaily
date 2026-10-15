package com.oeaon.hydrodaily.billing

import android.app.Activity

interface PurchaseFlowLauncher {
    suspend fun launchPremiumPurchase(activity: Activity): LaunchResult

    enum class LaunchResult {
        STARTED,
        STORE_UNAVAILABLE,
        PRODUCT_NOT_FOUND
    }
}
