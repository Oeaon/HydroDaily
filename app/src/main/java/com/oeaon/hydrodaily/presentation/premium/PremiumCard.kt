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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oeaon.hydrodaily.R

@Composable
fun PremiumCard(
    state: PremiumUiState,
    onBuy: () -> Unit,
    onRestore: () -> Unit
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
            Text(
                text = stringResource(if (state.isPremium) R.string.premium_active else R.string.premium_desc),
                style = MaterialTheme.typography.bodyMedium
            )
            if (!state.isPremium) {
                Button(
                    onClick = onBuy,
                    enabled = !state.isBusy && !state.isPending,
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
                            text = state.price?.let { stringResource(R.string.premium_buy, it) }
                                ?: stringResource(R.string.premium_buy_no_price),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                TextButton(
                    onClick = onRestore,
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.premium_restore))
                }
            }
            val status = when {
                state.isPending -> R.string.premium_pending
                else -> state.message?.text()
            }
            status?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.message?.isError == true) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
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
