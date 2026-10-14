package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class ObserveHydrationStatusUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val intakeRepository: IntakeRepository,
    private val observeCurrentDate: ObserveCurrentDateUseCase
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HydrationStatus> =
        observeCurrentDate().flatMapLatest { date ->
            combine(profileRepository.profile, intakeRepository.observe(date)) { profile, intake ->
                HydrationStatus(profile, intake)
            }
        }
}
