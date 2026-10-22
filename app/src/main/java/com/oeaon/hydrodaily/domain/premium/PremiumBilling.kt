package com.oeaon.hydrodaily.domain.premium

import kotlinx.coroutines.flow.Flow

interface PremiumBilling {
    val purchaseResults: Flow<PurchaseResult>
    suspend fun ownedPlan(): OwnedPlanQuery
    suspend fun prices(): Map<PremiumPlan, String>
}

enum class PremiumPlan {
    MONTHLY,
    LIFETIME
}

sealed interface OwnedPlanQuery {
    data class Answered(val plan: PremiumPlan?) : OwnedPlanQuery
    data object StoreUnavailable : OwnedPlanQuery
}

sealed interface PurchaseResult {
    data class Purchased(val plan: PremiumPlan) : PurchaseResult
    data object Pending : PurchaseResult
    data object Cancelled : PurchaseResult
    data object Failed : PurchaseResult
}
