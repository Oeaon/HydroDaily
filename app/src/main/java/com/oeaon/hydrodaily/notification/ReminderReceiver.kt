package com.oeaon.hydrodaily.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.model.Streak
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.domain.usecase.AddWaterUseCase
import com.oeaon.hydrodaily.domain.usecase.ObserveHydrationStatusUseCase
import com.oeaon.hydrodaily.domain.usecase.ObserveStreakUseCase
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
    @Inject lateinit var observeStreak: ObserveStreakUseCase
    @Inject lateinit var addWater: AddWaterUseCase
    @Inject lateinit var syncReminder: SyncReminderUseCase
    @Inject lateinit var analytics: AnalyticsTracker
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_DAILY_REMINDER && action != ACTION_QUICK_ADD) return
        val pendingResult = goAsync()
        scope.launch {
            try {
                if (action == ACTION_QUICK_ADD) {
                    quickAdd(intent.getIntExtra(EXTRA_AMOUNT_ML, DEFAULT_QUICK_ADD_ML))
                } else {
                    showReminder(context)
                    syncReminder()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun quickAdd(amountMl: Int) {
        notifier.dismiss()
        if (amountMl > 0) addWater(amountMl, fromNotification = true)
    }

    private suspend fun showReminder(context: Context) {
        val settings = settingsRepository.reminderSettings.first()
        if (!settings.enabled) return
        val status = observeHydrationStatus().first()
        if (!settings.shouldNotify(status)) return
        notifier.show(
            title = context.getString(R.string.notification_title),
            body = context.reminderBody(status, observeStreak().first()),
            quickAddMl = DEFAULT_QUICK_ADD_ML.takeIf { status.profile != null && !status.isGoalReached }
        )
        analytics.track(AnalyticsEvent.ReminderShown)
    }

    private fun ReminderSettings.shouldNotify(status: HydrationStatus): Boolean =
        !(skipWhenGoalReached && status.isGoalReached)

    private fun Context.reminderBody(status: HydrationStatus, streak: Streak): String {
        val consumed = formatVolume(status.intake.totalMl)
        val goal = formatVolume(status.goalMl)
        return when {
            status.profile == null -> getString(R.string.notification_body_no_profile)
            status.isGoalReached -> getString(R.string.notification_body_goal_reached)
            streak.current > 0 -> resources.getQuantityString(
                R.plurals.notification_body_streak,
                streak.current,
                consumed,
                goal,
                streak.current
            )
            else -> getString(R.string.notification_body_progress, consumed, goal)
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.oeaon.hydrodaily.action.DAILY_REMINDER"
        const val ACTION_QUICK_ADD = "com.oeaon.hydrodaily.action.QUICK_ADD"
        const val EXTRA_AMOUNT_ML = "amount_ml"
        private const val DEFAULT_QUICK_ADD_ML = 250
    }
}
