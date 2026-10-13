package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveHydrationStatusUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val intakeRepository: IntakeRepository,
    private val timeProvider: TimeProvider
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HydrationStatus> =
        currentDate().flatMapLatest { date ->
            combine(profileRepository.profile, intakeRepository.observe(date)) { profile, intake ->
                HydrationStatus(profile, intake)
            }
        }

    private fun currentDate() = flow {
        while (true) {
            emit(timeProvider.today())
            delay(DATE_CHECK_INTERVAL_MS)
        }
    }.distinctUntilChanged()

    private companion object {
        const val DATE_CHECK_INTERVAL_MS = 30_000L
    }
}
