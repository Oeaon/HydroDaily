package com.oeaon.hydrodaily.domain.premium

import kotlinx.coroutines.flow.Flow

interface PremiumBilling {
    val purchaseResults: Flow<PurchaseResult>
    suspend fun isPremiumOwned(): Boolean?
    suspend fun premiumPrice(): String?
}

enum class PurchaseResult {
    PURCHASED,
    PENDING,
    CANCELLED,
    FAILED
}
