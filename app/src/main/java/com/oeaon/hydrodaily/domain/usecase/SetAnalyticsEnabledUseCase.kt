package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import javax.inject.Inject

class SetAnalyticsEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setAnalyticsEnabled(enabled)
        analytics.setCollectionEnabled(enabled)
    }
}
