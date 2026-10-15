package com.oeaon.hydrodaily.ads

import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsPolicy @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationScope scope: CoroutineScope
) {

    val adsEnabled: StateFlow<Boolean> = settingsRepository.isPremium
        .map { premium -> !premium }
        .stateIn(scope, SharingStarted.Eagerly, false)

    suspend fun awaitAdsEnabled(): Boolean = !settingsRepository.isPremium.first()
}
