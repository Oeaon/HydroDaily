package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakePremiumBilling
import com.oeaon.hydrodaily.domain.fakes.FakeSettingsRepository
import com.oeaon.hydrodaily.domain.premium.OwnedPlanQuery
import com.oeaon.hydrodaily.domain.premium.PremiumPlan
import com.oeaon.hydrodaily.domain.premium.PurchaseResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumUseCasesTest {

    private val settings = FakeSettingsRepository()
    private val billing = FakePremiumBilling()
    private val analytics = FakeAnalyticsTracker()
    private val sync = SyncPremiumUseCase(billing, settings)
    private val handle = HandlePurchaseResultUseCase(settings, analytics)

    @Test
    fun `sync stores the plan reported by the store`() = runTest {
        billing.owned = OwnedPlanQuery.Answered(PremiumPlan.MONTHLY)
        sync()
        assertEquals(PremiumPlan.MONTHLY, settings.premiumState.value)
        assertTrue(settings.isPremium.first())

        billing.owned = OwnedPlanQuery.Answered(null)
        sync()
        assertNull(settings.premiumState.value)
        assertFalse(settings.isPremium.first())
    }

    @Test
    fun `sync keeps the cached plan when the store is unavailable`() = runTest {
        settings.premiumState.value = PremiumPlan.LIFETIME
        billing.owned = OwnedPlanQuery.StoreUnavailable
        assertEquals(OwnedPlanQuery.StoreUnavailable, sync())
        assertEquals(PremiumPlan.LIFETIME, settings.premiumState.value)
    }

    @Test
    fun `a completed purchase unlocks its plan once`() = runTest {
        handle(PurchaseResult.Purchased(PremiumPlan.MONTHLY))
        handle(PurchaseResult.Purchased(PremiumPlan.MONTHLY))
        assertEquals(PremiumPlan.MONTHLY, settings.premiumState.value)
        assertEquals(1, analytics.events.count { it is AnalyticsEvent.PremiumPurchased })
    }

    @Test
    fun `lifetime replaces monthly but never the other way round`() = runTest {
        handle(PurchaseResult.Purchased(PremiumPlan.MONTHLY))
        handle(PurchaseResult.Purchased(PremiumPlan.LIFETIME))
        assertEquals(PremiumPlan.LIFETIME, settings.premiumState.value)
        handle(PurchaseResult.Purchased(PremiumPlan.MONTHLY))
        assertEquals(PremiumPlan.LIFETIME, settings.premiumState.value)
    }

    @Test
    fun `pending, cancelled or failed purchases do not unlock premium`() = runTest {
        listOf(PurchaseResult.Pending, PurchaseResult.Cancelled, PurchaseResult.Failed).forEach { handle(it) }
        assertNull(settings.premiumState.value)
    }
}
