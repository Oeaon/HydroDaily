package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddWaterUseCase @Inject constructor(
    private val repository: IntakeRepository,
    private val timeProvider: TimeProvider,
    private val updateGoalHistory: UpdateGoalHistoryUseCase,
    private val observeStreak: ObserveStreakUseCase,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke(amountMl: Int, fromNotification: Boolean = false): Result {
        require(amountMl > 0)
        repository.add(timeProvider.today(), amountMl)
        analytics.track(AnalyticsEvent.WaterAdded(amountMl, fromNotification))
        if (!updateGoalHistory()) return Result(goalJustReached = false, streakDays = 0)

        val streakDays = observeStreak().first().current
        analytics.track(AnalyticsEvent.GoalReached(streakDays))
        return Result(goalJustReached = true, streakDays = streakDays)
    }

    data class Result(val goalJustReached: Boolean, val streakDays: Int)
}
