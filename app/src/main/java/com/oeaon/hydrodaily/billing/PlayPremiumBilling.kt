package com.oeaon.hydrodaily.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.oeaon.hydrodaily.di.ApplicationScope
import com.oeaon.hydrodaily.domain.premium.OwnedPlanQuery
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
import com.oeaon.hydrodaily.domain.premium.PremiumPlan
import com.oeaon.hydrodaily.domain.premium.PurchaseResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PlayPremiumBilling @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope private val scope: CoroutineScope
) : PremiumBilling, PurchaseFlowLauncher, PurchasesUpdatedListener {

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val connectionMutex = Mutex()
    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = RESULT_BUFFER)
    override val purchaseResults: Flow<PurchaseResult> = _purchaseResults.asSharedFlow()

    private val cachedProducts = mutableMapOf<PremiumPlan, ProductDetails>()

    override suspend fun ownedPlan(): OwnedPlanQuery {
        if (!ensureConnected()) return OwnedPlanQuery.StoreUnavailable
        val lifetime = ownedPurchases(ProductType.INAPP) ?: return OwnedPlanQuery.StoreUnavailable
        if (lifetime.any { it.plan() == PremiumPlan.LIFETIME }) return OwnedPlanQuery.Answered(PremiumPlan.LIFETIME)
        val subscriptions = ownedPurchases(ProductType.SUBS) ?: return OwnedPlanQuery.StoreUnavailable
        val monthly = subscriptions.any { it.plan() == PremiumPlan.MONTHLY }
        return OwnedPlanQuery.Answered(PremiumPlan.MONTHLY.takeIf { monthly })
    }

    override suspend fun prices(): Map<PremiumPlan, String> =
        PremiumPlan.entries.mapNotNull { plan -> loadProduct(plan)?.formattedPrice(plan)?.let { plan to it } }.toMap()

    override suspend fun launchPurchase(activity: Activity, plan: PremiumPlan): PurchaseFlowLauncher.LaunchResult {
        if (!ensureConnected()) return PurchaseFlowLauncher.LaunchResult.STORE_UNAVAILABLE
        val product = loadProduct(plan) ?: return PurchaseFlowLauncher.LaunchResult.PRODUCT_NOT_FOUND
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product)
        if (plan == PremiumPlan.MONTHLY) {
            val offerToken = product.baseOffer()?.offerToken ?: return PurchaseFlowLauncher.LaunchResult.PRODUCT_NOT_FOUND
            productParams.setOfferToken(offerToken)
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams.build()))
            .build()
        val result = client.launchBillingFlow(activity, params)
        return if (result.responseCode == BillingResponseCode.OK) {
            PurchaseFlowLauncher.LaunchResult.STARTED
        } else {
            PurchaseFlowLauncher.LaunchResult.STORE_UNAVAILABLE
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            val outcome = when (result.responseCode) {
                BillingResponseCode.OK -> handlePurchases(purchases.orEmpty())
                BillingResponseCode.ITEM_ALREADY_OWNED -> alreadyOwned()
                BillingResponseCode.USER_CANCELED -> PurchaseResult.Cancelled
                else -> PurchaseResult.Failed
            }
            _purchaseResults.emit(outcome)
        }
    }

    private suspend fun alreadyOwned(): PurchaseResult {
        val plan = (ownedPlan() as? OwnedPlanQuery.Answered)?.plan ?: return PurchaseResult.Failed
        return PurchaseResult.Purchased(plan)
    }

    private suspend fun handlePurchases(purchases: List<Purchase>): PurchaseResult {
        val premium = purchases.firstOrNull { it.plan() != null } ?: return PurchaseResult.Failed
        return when (premium.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                acknowledgeIfNeeded(premium)
                PurchaseResult.Purchased(requireNotNull(premium.plan()))
            }
            Purchase.PurchaseState.PENDING -> PurchaseResult.Pending
            else -> PurchaseResult.Failed
        }
    }

    private suspend fun ownedPurchases(type: String): List<Purchase>? {
        val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
        val (result, purchases) = withTimeoutOrNull(STORE_TIMEOUT_MS) {
            suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { continuation ->
                client.queryPurchasesAsync(params) { result, purchases ->
                    if (continuation.isActive) continuation.resume(result to purchases)
                }
            }
        } ?: return null
        if (result.responseCode != BillingResponseCode.OK) return null
        val owned = purchases.filter { it.plan() != null && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        owned.forEach { acknowledgeIfNeeded(it) }
        return owned
    }

    private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        withTimeoutOrNull(STORE_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                client.acknowledgePurchase(params) { if (continuation.isActive) continuation.resume(Unit) }
            }
        }
    }

    private suspend fun loadProduct(plan: PremiumPlan): ProductDetails? {
        cachedProducts[plan]?.let { return it }
        if (!ensureConnected()) return null
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(plan.productId)
                        .setProductType(plan.productType)
                        .build()
                )
            )
            .build()
        val product = withTimeoutOrNull(STORE_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                client.queryProductDetailsAsync(params) { result, details ->
                    val found = details.productDetailsList.firstOrNull()
                        .takeIf { result.responseCode == BillingResponseCode.OK }
                    if (continuation.isActive) continuation.resume(found)
                }
            }
        }
        product?.let { cachedProducts[plan] = it }
        return product
    }

    private fun ProductDetails.formattedPrice(plan: PremiumPlan): String? = when (plan) {
        PremiumPlan.LIFETIME -> oneTimePurchaseOfferDetails?.formattedPrice
        PremiumPlan.MONTHLY -> baseOffer()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
    }

    private fun ProductDetails.baseOffer(): ProductDetails.SubscriptionOfferDetails? =
        subscriptionOfferDetails?.let { offers -> offers.firstOrNull { it.offerId == null } ?: offers.firstOrNull() }

    private suspend fun ensureConnected(): Boolean = connectionMutex.withLock {
        if (client.isReady) return@withLock true
        withTimeoutOrNull(STORE_TIMEOUT_MS) { startConnection() } ?: false
    }

    private suspend fun startConnection(): Boolean =
        suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) continuation.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) continuation.resume(false)
                }
            })
        }

    private fun Purchase.plan(): PremiumPlan? = PremiumPlan.entries.firstOrNull { it.productId in products }

    private val PremiumPlan.productId: String
        get() = when (this) {
            PremiumPlan.MONTHLY -> MONTHLY_PRODUCT_ID
            PremiumPlan.LIFETIME -> LIFETIME_PRODUCT_ID
        }

    private val PremiumPlan.productType: String
        get() = when (this) {
            PremiumPlan.MONTHLY -> ProductType.SUBS
            PremiumPlan.LIFETIME -> ProductType.INAPP
        }

    companion object {
        const val MONTHLY_PRODUCT_ID = "premium_monthly"
        const val LIFETIME_PRODUCT_ID = "premium_lifetime"
        private const val RESULT_BUFFER = 4
        private const val STORE_TIMEOUT_MS = 10_000L
    }
}
