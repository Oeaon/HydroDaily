package com.oeaon.hydrodaily.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
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
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
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

    private var cachedProduct: ProductDetails? = null

    override suspend fun isPremiumOwned(): Boolean? {
        if (!ensureConnected()) return null
        val (result, purchases) = queryPurchases()
        if (result.responseCode != BillingResponseCode.OK) return null
        val owned = purchases.filter { it.isPremium() && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        owned.forEach { acknowledgeIfNeeded(it) }
        return owned.isNotEmpty()
    }

    override suspend fun premiumPrice(): String? =
        loadProduct()?.oneTimePurchaseOfferDetails?.formattedPrice

    override suspend fun launchPremiumPurchase(activity: Activity): PurchaseFlowLauncher.LaunchResult {
        if (!ensureConnected()) return PurchaseFlowLauncher.LaunchResult.STORE_UNAVAILABLE
        val product = loadProduct() ?: return PurchaseFlowLauncher.LaunchResult.PRODUCT_NOT_FOUND
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).build())
            )
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
                BillingResponseCode.ITEM_ALREADY_OWNED ->
                    if (isPremiumOwned() == true) PurchaseResult.PURCHASED else PurchaseResult.FAILED
                BillingResponseCode.USER_CANCELED -> PurchaseResult.CANCELLED
                else -> PurchaseResult.FAILED
            }
            _purchaseResults.emit(outcome)
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>): PurchaseResult {
        val premium = purchases.firstOrNull { it.isPremium() } ?: return PurchaseResult.FAILED
        return when (premium.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                acknowledgeIfNeeded(premium)
                PurchaseResult.PURCHASED
            }
            Purchase.PurchaseState.PENDING -> PurchaseResult.PENDING
            else -> PurchaseResult.FAILED
        }
    }

    private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        suspendCancellableCoroutine { continuation ->
            client.acknowledgePurchase(params) { continuation.resume(Unit) }
        }
    }

    private suspend fun loadProduct(): ProductDetails? {
        cachedProduct?.let { return it }
        if (!ensureConnected()) return null
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { result, details ->
                val product = details.productDetailsList.firstOrNull()
                    .takeIf { result.responseCode == BillingResponseCode.OK }
                cachedProduct = product
                continuation.resume(product)
            }
        }
    }

    private suspend fun queryPurchases(): Pair<BillingResult, List<Purchase>> {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        return suspendCancellableCoroutine { continuation ->
            client.queryPurchasesAsync(params) { result, purchases -> continuation.resume(result to purchases) }
        }
    }

    private suspend fun ensureConnected(): Boolean = connectionMutex.withLock {
        if (client.isReady) return@withLock true
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
    }

    private fun Purchase.isPremium(): Boolean = PREMIUM_PRODUCT_ID in products

    companion object {
        const val PREMIUM_PRODUCT_ID = "premium_no_ads"
        private const val RESULT_BUFFER = 4
    }
}
