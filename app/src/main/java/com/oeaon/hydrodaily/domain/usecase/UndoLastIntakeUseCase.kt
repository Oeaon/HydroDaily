package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import javax.inject.Inject

class UndoLastIntakeUseCase @Inject constructor(
    private val repository: IntakeRepository,
    private val timeProvider: TimeProvider,
    private val updateGoalHistory: UpdateGoalHistoryUseCase,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke() {
        repository.removeLast(timeProvider.today())
        updateGoalHistory()
        analytics.track(AnalyticsEvent.WaterUndone)
    }
}
