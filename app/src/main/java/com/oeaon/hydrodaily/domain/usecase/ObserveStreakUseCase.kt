package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.Streak
import com.oeaon.hydrodaily.domain.repository.GoalHistoryRepository
import com.oeaon.hydrodaily.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ObserveStreakUseCase @Inject constructor(
    private val goalHistoryRepository: GoalHistoryRepository,
    private val observeCurrentDate: ObserveCurrentDateUseCase,
    private val calculator: StreakCalculator
) {

    operator fun invoke(): Flow<Streak> =
        combine(goalHistoryRepository.goalDays, observeCurrentDate()) { goalDays, today ->
            calculator.calculate(goalDays, today)
        }.distinctUntilChanged()
}
