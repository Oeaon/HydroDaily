package com.oeaon.hydrodaily

import android.app.Application
import com.oeaon.hydrodaily.ads.AppOpenAdManager
import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.domain.usecase.SyncReminderUseCase
import com.oeaon.hydrodaily.notification.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HydroDailyApp : Application() {

    @Inject lateinit var notifier: ReminderNotifier
    @Inject lateinit var syncReminder: SyncReminderUseCase
    @Inject lateinit var appOpenAdManager: AppOpenAdManager
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var analytics: AnalyticsTracker
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        notifier.createChannel()
        appOpenAdManager.register()
        settingsRepository.analyticsEnabled
            .onEach(analytics::setCollectionEnabled)
            .launchIn(scope)
        scope.launch { syncReminder() }
    }
}
