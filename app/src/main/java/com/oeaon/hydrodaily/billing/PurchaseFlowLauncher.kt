package com.oeaon.hydrodaily.billing

import android.app.Activity
import com.oeaon.hydrodaily.domain.premium.PremiumPlan

interface PurchaseFlowLauncher {
    suspend fun launchPurchase(activity: Activity, plan: PremiumPlan): LaunchResult

    enum class LaunchResult {
        STARTED,
        STORE_UNAVAILABLE,
        PRODUCT_NOT_FOUND
    }
}
