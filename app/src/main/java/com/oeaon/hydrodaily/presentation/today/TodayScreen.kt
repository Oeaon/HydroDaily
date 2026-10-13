package com.oeaon.hydrodaily.presentation.today

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.presentation.common.formatVolume
import com.oeaon.hydrodaily.presentation.components.WaterProgressRing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val QuickAmountsMl = listOf(150, 250, 330, 500)

@Composable
fun TodayRoute(
    onSetUpProfile: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playDrop = rememberDropSound()
    val haptics = LocalHapticFeedback.current

    TodayScreen(
        state = state,
        contentPadding = contentPadding,
        onAddWater = { amount ->
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            playDrop()
            viewModel.onAddWater(amount)
        },
        onUndo = viewModel::onUndo,
        onSetUpProfile = onSetUpProfile
    )
}

@Composable
fun TodayScreen(
    state: TodayUiState,
    contentPadding: PaddingValues,
    onAddWater: (Int) -> Unit,
    onUndo: () -> Unit,
    onSetUpProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Header()
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(280.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.hasProfile -> SetUpProfileCard(onSetUpProfile)
            else -> {
                ProgressSection(state)
                StatsRow(state)
                QuickAddSection(canUndo = state.canUndo, onAddWater = onAddWater, onUndo = onUndo)
            }
        }
    }
}

@Composable
private fun Header() {
    val date = remember {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
            .replaceFirstChar { it.uppercase() }
    }
    Column {
        Text(
            text = date,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.today_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ProgressSection(state: TodayUiState) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WaterProgressRing(
            progress = state.progress,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                AnimatedContent(
                    targetState = state.consumedMl,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "consumed"
                ) { consumed ->
                    Text(
                        text = context.formatVolume(consumed),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.today_of_goal, context.formatVolume(state.goalMl)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(R.string.percent_value, state.percent),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Text(
            text = if (state.isGoalReached) {
                stringResource(R.string.today_goal_reached)
            } else {
                stringResource(R.string.today_remaining, context.formatVolume(state.remainingMl))
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.isGoalReached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsRow(state: TodayUiState) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile(stringResource(R.string.stat_goal), context.formatVolume(state.goalMl), Modifier.weight(1f))
        StatTile(stringResource(R.string.stat_remaining), context.formatVolume(state.remainingMl), Modifier.weight(1f))
        StatTile(stringResource(R.string.stat_entries), state.entriesCount.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickAddSection(canUndo: Boolean, onAddWater: (Int) -> Unit, onUndo: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.today_add_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onUndo, enabled = canUndo) {
                Icon(painterResource(R.drawable.ic_undo), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.today_undo))
            }
        }
        QuickAmountsMl.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { amount ->
                    QuickAddCard(amount, onAddWater, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickAddCard(amountMl: Int, onAddWater: (Int) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ElevatedCard(
        onClick = { onAddWater(amountMl) },
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(dropSizeFor(amountMl))
                )
            }
            Column {
                Text(
                    text = "+" + context.formatVolume(amountMl),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun dropSizeFor(amountMl: Int): Dp = when {
    amountMl <= 150 -> 16.dp
    amountMl <= 250 -> 19.dp
    amountMl <= 330 -> 22.dp
    else -> 24.dp
}

@Composable
private fun SetUpProfileCard(onSetUpProfile: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_water_drop),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(R.string.today_no_profile_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.today_no_profile_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Button(onClick = onSetUpProfile) {
                Text(stringResource(R.string.today_no_profile_action))
            }
        }
    }
}


@Composable
private fun rememberDropSound(): () -> Unit {
    val context = LocalContext.current
    val player = remember { MediaPlayer.create(context, R.raw.water_drop_sound_btn) }
    DisposableEffect(player) {
        onDispose { player?.release() }
    }
    return remember(player) {
        {
            player?.run {
                if (isPlaying) seekTo(0) else start()
            }
        }
    }
}
