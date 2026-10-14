package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.repository.GoalHistoryRepository
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateGoalHistoryUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val intakeRepository: IntakeRepository,
    private val goalHistoryRepository: GoalHistoryRepository,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(): Boolean {
        val today = timeProvider.today()
        val status = HydrationStatus(profileRepository.profile.first(), intakeRepository.observe(today).first())
        val wasReached = today in goalHistoryRepository.goalDays.first()
        if (status.isGoalReached != wasReached) {
            goalHistoryRepository.setGoalReached(today, status.isGoalReached)
        }
        return status.isGoalReached && !wasReached
    }
}
