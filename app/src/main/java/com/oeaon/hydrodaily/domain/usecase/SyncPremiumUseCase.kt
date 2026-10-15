package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.premium.OwnedPlanQuery
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import javax.inject.Inject

class SyncPremiumUseCase @Inject constructor(
    private val billing: PremiumBilling,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(): OwnedPlanQuery {
        val query = billing.ownedPlan()
        if (query is OwnedPlanQuery.Answered) settingsRepository.setPremiumPlan(query.plan)
        return query
    }
}
