package com.oeaon.hydrodaily.presentation.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.billing.PurchaseFlowLauncher
import com.oeaon.hydrodaily.domain.premium.OwnedPlanQuery
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
import com.oeaon.hydrodaily.domain.premium.PremiumPlan
import com.oeaon.hydrodaily.domain.premium.PurchaseResult
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.domain.usecase.SyncPremiumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val billing: PremiumBilling,
    private val purchaseFlowLauncher: PurchaseFlowLauncher,
    private val syncPremium: SyncPremiumUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.premiumPlan
            .onEach { plan -> _uiState.update { it.copy(activePlan = plan) } }
            .launchIn(viewModelScope)
        billing.purchaseResults
            .onEach(::onPurchaseResult)
            .launchIn(viewModelScope)
        viewModelScope.launch {
            val prices = billing.prices()
            _uiState.update { it.copy(prices = prices) }
        }
    }

    fun onBuy(activity: Activity, plan: PremiumPlan) {
        if (_uiState.value.isBusy || _uiState.value.activePlan != null) return
        _uiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            val message = when (purchaseFlowLauncher.launchPurchase(activity, plan)) {
                PurchaseFlowLauncher.LaunchResult.STARTED -> return@launch
                PurchaseFlowLauncher.LaunchResult.STORE_UNAVAILABLE -> PremiumMessage.STORE_UNAVAILABLE
                PurchaseFlowLauncher.LaunchResult.PRODUCT_NOT_FOUND -> PremiumMessage.PRODUCT_NOT_FOUND
            }
            _uiState.update { it.copy(isBusy = false, message = message) }
        }
    }

    fun onRestore() {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            val message = when (val query = syncPremium()) {
                OwnedPlanQuery.StoreUnavailable -> PremiumMessage.STORE_UNAVAILABLE
                is OwnedPlanQuery.Answered ->
                    if (query.plan != null) PremiumMessage.RESTORED else PremiumMessage.NOTHING_TO_RESTORE
            }
            _uiState.update { it.copy(isBusy = false, message = message) }
        }
    }

    private fun onPurchaseResult(result: PurchaseResult) {
        _uiState.update {
            it.copy(
                isBusy = false,
                isPending = result == PurchaseResult.Pending,
                message = if (result == PurchaseResult.Failed) PremiumMessage.PURCHASE_FAILED else null
            )
        }
    }
}

data class PremiumUiState(
    val activePlan: PremiumPlan? = null,
    val prices: Map<PremiumPlan, String> = emptyMap(),
    val isBusy: Boolean = false,
    val isPending: Boolean = false,
    val message: PremiumMessage? = null
) {
    val isPremium: Boolean get() = activePlan != null
}

enum class PremiumMessage {
    STORE_UNAVAILABLE,
    PRODUCT_NOT_FOUND,
    PURCHASE_FAILED,
    NOTHING_TO_RESTORE,
    RESTORED
}
