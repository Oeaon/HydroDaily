package com.oeaon.hydrodaily.presentation.premium

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.domain.premium.PremiumPlan

@Composable
fun PremiumCard(
    state: PremiumUiState,
    onBuy: (PremiumPlan) -> Unit,
    onRestore: () -> Unit,
    onManageSubscription: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_water_drop),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.premium_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            when (state.activePlan) {
                PremiumPlan.LIFETIME -> Text(
                    text = stringResource(R.string.premium_active),
                    style = MaterialTheme.typography.bodyMedium
                )
                PremiumPlan.MONTHLY -> {
                    Text(
                        text = stringResource(R.string.premium_active_monthly),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(onClick = onManageSubscription, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.premium_manage_subscription))
                    }
                }
                null -> PurchaseOptions(state, onBuy, onRestore)
            }
            val status = if (state.isPending) R.string.premium_pending else state.message?.text()
            status?.let { StatusMessage(it, isError = state.message?.isError == true) }
        }
    }
}

@Composable
private fun PurchaseOptions(state: PremiumUiState, onBuy: (PremiumPlan) -> Unit, onRestore: () -> Unit) {
    val enabled = !state.isBusy && !state.isPending
    Text(
        text = stringResource(R.string.premium_desc),
        style = MaterialTheme.typography.bodyMedium
    )
    Button(
        onClick = { onBuy(PremiumPlan.LIFETIME) },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (state.isBusy) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = state.prices[PremiumPlan.LIFETIME]?.let { stringResource(R.string.premium_buy_lifetime, it) }
                    ?: stringResource(R.string.premium_buy_lifetime_no_price),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    OutlinedButton(
        onClick = { onBuy(PremiumPlan.MONTHLY) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text = state.prices[PremiumPlan.MONTHLY]?.let { stringResource(R.string.premium_buy_monthly, it) }
                ?: stringResource(R.string.premium_buy_monthly_no_price),
            style = MaterialTheme.typography.titleMedium
        )
    }
    Text(
        text = stringResource(R.string.premium_subscription_terms),
        style = MaterialTheme.typography.bodySmall
    )
    TextButton(
        onClick = onRestore,
        enabled = !state.isBusy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.premium_restore))
    }
}

@Composable
private fun StatusMessage(@StringRes text: Int, isError: Boolean) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@StringRes
private fun PremiumMessage.text(): Int = when (this) {
    PremiumMessage.STORE_UNAVAILABLE -> R.string.premium_error_store
    PremiumMessage.PRODUCT_NOT_FOUND -> R.string.premium_error_product
    PremiumMessage.PURCHASE_FAILED -> R.string.premium_error_failed
    PremiumMessage.NOTHING_TO_RESTORE -> R.string.premium_nothing_to_restore
    PremiumMessage.RESTORED -> R.string.premium_restored
}

private val PremiumMessage.isError: Boolean
    get() = this != PremiumMessage.RESTORED
