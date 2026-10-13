package com.oeaon.hydrodaily

import android.app.Application
import com.oeaon.hydrodaily.ads.AppOpenAdManager
import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.usecase.SyncReminderUseCase
import com.oeaon.hydrodaily.notification.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HydroDailyApp : Application() {

    @Inject lateinit var notifier: ReminderNotifier
    @Inject lateinit var syncReminder: SyncReminderUseCase
    @Inject lateinit var appOpenAdManager: AppOpenAdManager
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        notifier.createChannel()
        appOpenAdManager.register()
        scope.launch { syncReminder() }
    }
}
