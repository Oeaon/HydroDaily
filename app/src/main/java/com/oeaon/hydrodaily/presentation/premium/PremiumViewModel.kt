package com.oeaon.hydrodaily.presentation.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.billing.PurchaseFlowLauncher
import com.oeaon.hydrodaily.domain.premium.PremiumBilling
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
        settingsRepository.isPremium
            .onEach { premium -> _uiState.update { it.copy(isPremium = premium) } }
            .launchIn(viewModelScope)
        billing.purchaseResults
            .onEach(::onPurchaseResult)
            .launchIn(viewModelScope)
        viewModelScope.launch {
            val price = billing.premiumPrice()
            _uiState.update { it.copy(price = price) }
        }
    }

    fun onBuy(activity: Activity) {
        if (_uiState.value.isBusy || _uiState.value.isPremium) return
        _uiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            val message = when (purchaseFlowLauncher.launchPremiumPurchase(activity)) {
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
            val message = when (syncPremium()) {
                true -> PremiumMessage.RESTORED
                false -> PremiumMessage.NOTHING_TO_RESTORE
                null -> PremiumMessage.STORE_UNAVAILABLE
            }
            _uiState.update { it.copy(isBusy = false, message = message) }
        }
    }

    private fun onPurchaseResult(result: PurchaseResult) {
        _uiState.update {
            it.copy(
                isBusy = false,
                isPending = result == PurchaseResult.PENDING,
                message = if (result == PurchaseResult.FAILED) PremiumMessage.PURCHASE_FAILED else null
            )
        }
    }
}

data class PremiumUiState(
    val isPremium: Boolean = false,
    val price: String? = null,
    val isBusy: Boolean = false,
    val isPending: Boolean = false,
    val message: PremiumMessage? = null
)

enum class PremiumMessage {
    STORE_UNAVAILABLE,
    PRODUCT_NOT_FOUND,
    PURCHASE_FAILED,
    NOTHING_TO_RESTORE,
    RESTORED
}
