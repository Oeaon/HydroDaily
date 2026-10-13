package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import javax.inject.Inject

class UndoLastIntakeUseCase @Inject constructor(
    private val repository: IntakeRepository,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke() {
        repository.removeLast(timeProvider.today())
    }
}
