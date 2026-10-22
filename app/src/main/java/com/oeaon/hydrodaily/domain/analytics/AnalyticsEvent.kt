package com.oeaon.hydrodaily.domain.analytics

import com.oeaon.hydrodaily.domain.premium.PremiumPlan

sealed class AnalyticsEvent(val name: String, val params: Map<String, Any> = emptyMap()) {

    class WaterAdded(amountMl: Int, fromNotification: Boolean) : AnalyticsEvent(
        "water_added",
        mapOf("amount_ml" to amountMl, "source" to if (fromNotification) "notification" else "app")
    )

    data object WaterUndone : AnalyticsEvent("water_undone")

    class GoalReached(streakDays: Int) : AnalyticsEvent("goal_reached", mapOf("streak_days" to streakDays))

    class ProfileSaved(dailyGoalMl: Int) : AnalyticsEvent("profile_saved", mapOf("daily_goal_ml" to dailyGoalMl))

    class RemindersUpdated(enabled: Boolean, remindersPerDay: Int, activeDays: Int) : AnalyticsEvent(
        "reminders_updated",
        mapOf("enabled" to enabled.toString(), "per_day" to remindersPerDay, "active_days" to activeDays)
    )

    data object ReminderShown : AnalyticsEvent("reminder_shown")

    data object ReviewPrompted : AnalyticsEvent("review_prompted")

    class PremiumPurchased(plan: PremiumPlan) :
        AnalyticsEvent("premium_purchased", mapOf("plan" to plan.name.lowercase()))
}
