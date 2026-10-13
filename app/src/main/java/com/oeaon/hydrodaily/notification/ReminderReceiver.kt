package com.oeaon.hydrodaily.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.domain.usecase.ObserveHydrationStatusUseCase
import com.oeaon.hydrodaily.domain.usecase.SyncReminderUseCase
import com.oeaon.hydrodaily.presentation.common.formatVolume
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: ReminderNotifier
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var observeHydrationStatus: ObserveHydrationStatusUseCase
    @Inject lateinit var syncReminder: SyncReminderUseCase
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_REMINDER) return
        val pendingResult = goAsync()
        scope.launch {
            try {
                if (settingsRepository.remindersEnabled.first()) {
                    val status = observeHydrationStatus().first()
                    notifier.show(
                        title = context.getString(R.string.notification_title),
                        body = context.reminderBody(status)
                    )
                }
                syncReminder()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun Context.reminderBody(status: HydrationStatus): String = when {
        status.profile == null -> getString(R.string.notification_body_no_profile)
        status.isGoalReached -> getString(R.string.notification_body_goal_reached)
        else -> getString(
            R.string.notification_body_progress,
            formatVolume(status.intake.totalMl),
            formatVolume(status.goalMl)
        )
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.oeaon.hydrodaily.action.DAILY_REMINDER"
    }
}
