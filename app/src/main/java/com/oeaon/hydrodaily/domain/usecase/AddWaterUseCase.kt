package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import javax.inject.Inject

class AddWaterUseCase @Inject constructor(
    private val repository: IntakeRepository,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(amountMl: Int) {
        require(amountMl > 0)
        repository.add(timeProvider.today(), amountMl)
    }
}
