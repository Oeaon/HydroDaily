package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClaimReviewPromptUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke(streakDays: Int): Boolean {
        if (streakDays < MIN_STREAK_DAYS) return false
        val today = timeProvider.today()
        val lastPrompt = settingsRepository.lastReviewPrompt.first()
        if (lastPrompt != null && today.isBefore(lastPrompt.plusDays(DAYS_BETWEEN_PROMPTS))) return false
        settingsRepository.setLastReviewPrompt(today)
        analytics.track(AnalyticsEvent.ReviewPrompted)
        return true
    }

    companion object {
        const val MIN_STREAK_DAYS = 3
        const val DAYS_BETWEEN_PROMPTS = 60L
    }
}
