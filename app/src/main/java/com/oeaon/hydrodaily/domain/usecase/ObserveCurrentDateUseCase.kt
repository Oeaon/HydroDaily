package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveCurrentDateUseCase @Inject constructor(
    private val timeProvider: TimeProvider
) {

    operator fun invoke(): Flow<LocalDate> = flow {
        while (true) {
            emit(timeProvider.today())
            delay(DATE_CHECK_INTERVAL_MS)
        }
    }.distinctUntilChanged()

    private companion object {
        const val DATE_CHECK_INTERVAL_MS = 30_000L
    }
}
