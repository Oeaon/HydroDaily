package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.premium.PurchaseResult
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HandlePurchaseResultUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke(result: PurchaseResult) {
        if (result != PurchaseResult.PURCHASED || settingsRepository.isPremium.first()) return
        settingsRepository.setPremium(true)
        analytics.track(AnalyticsEvent.PremiumPurchased)
    }
}
