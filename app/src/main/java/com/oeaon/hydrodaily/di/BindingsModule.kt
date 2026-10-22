package com.oeaon.hydrodaily.di

import com.oeaon.hydrodaily.billing.PlayPremiumBilling
import com.oeaon.hydrodaily.billing.PurchaseFlowLauncher
import com.oeaon.hydrodaily.data.analytics.FirebaseAnalyticsTracker
import com.oeaon.hydrodaily.data.repository.DataStoreGoalHistoryRepository
import com.oeaon.hydrodaily.data.repository.DataStoreIntakeRepository
import com.oeaon.hydrodaily.data.repository.DataStoreProfileRepository
import com.oeaon.hydrodaily.data.repository.DataStoreSettingsRepository
import com.oeaon.hydrodaily.data.time.SystemTimeProvider
import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
import com.oeaon.hydrodaily.domain.reminder.ReminderScheduler
import com.oeaon.hydrodaily.domain.repository.GoalHistoryRepository
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.notification.AlarmReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    abstract fun bindProfileRepository(impl: DataStoreProfileRepository): ProfileRepository

    @Binds
    abstract fun bindIntakeRepository(impl: DataStoreIntakeRepository): IntakeRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindGoalHistoryRepository(impl: DataStoreGoalHistoryRepository): GoalHistoryRepository

    @Binds
    abstract fun bindAnalyticsTracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker

    @Binds
    abstract fun bindPremiumBilling(impl: PlayPremiumBilling): PremiumBilling

    @Binds
    abstract fun bindPurchaseFlowLauncher(impl: PlayPremiumBilling): PurchaseFlowLauncher

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    abstract fun bindReminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
}
