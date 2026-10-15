package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakePremiumBilling
import com.oeaon.hydrodaily.domain.fakes.FakeSettingsRepository
import com.oeaon.hydrodaily.domain.premium.PurchaseResult
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
    fun `sync stores the ownership reported by the store`() = runTest {
        billing.owned = true
        assertEquals(true, sync())
        assertTrue(settings.premiumState.value)

        billing.owned = false
        assertEquals(false, sync())
        assertFalse(settings.premiumState.value)
    }

    @Test
    fun `sync keeps the cached value when the store is unavailable`() = runTest {
        settings.premiumState.value = true
        billing.owned = null
        assertNull(sync())
        assertTrue(settings.premiumState.value)
    }

    @Test
    fun `a completed purchase unlocks premium once`() = runTest {
        handle(PurchaseResult.PURCHASED)
        handle(PurchaseResult.PURCHASED)
        assertTrue(settings.premiumState.value)
        assertEquals(1, analytics.events.count { it is AnalyticsEvent.PremiumPurchased })
    }

    @Test
    fun `pending, cancelled or failed purchases do not unlock premium`() = runTest {
        listOf(PurchaseResult.PENDING, PurchaseResult.CANCELLED, PurchaseResult.FAILED).forEach { handle(it) }
        assertFalse(settings.premiumState.value)
    }
}
