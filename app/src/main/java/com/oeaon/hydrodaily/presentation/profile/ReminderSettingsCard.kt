package com.oeaon.hydrodaily.presentation.profile

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.domain.model.ReminderFrequency
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.presentation.common.formatTime
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Immutable
data class ReminderActions(
    val onEnabledChange: (Boolean) -> Unit,
    val onStartTimeChange: (LocalTime) -> Unit,
    val onEndTimeChange: (LocalTime) -> Unit,
    val onFrequencyChange: (ReminderFrequency) -> Unit,
    val onDayToggle: (DayOfWeek) -> Unit,
    val onSkipWhenGoalReachedChange: (Boolean) -> Unit,
    val onOpenSoundSettings: () -> Unit,
    val onOpenNotificationSettings: () -> Unit,
    val onOpenExactAlarmSettings: () -> Unit
)

@Composable
fun ReminderSettingsCard(
    settings: ReminderSettings,
    notificationsBlocked: Boolean,
    exactAlarmBlocked: Boolean,
    actions: ReminderActions
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notification),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_reminders), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = reminderSummary(settings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = settings.enabled, onCheckedChange = actions.onEnabledChange)
        }

        when {
            !settings.enabled -> Unit
            notificationsBlocked -> SettingsWarning(
                text = R.string.settings_notifications_blocked,
                isError = true,
                onAction = actions.onOpenNotificationSettings
            )
            exactAlarmBlocked -> SettingsWarning(
                text = R.string.settings_exact_alarm,
                isError = false,
                onAction = actions.onOpenExactAlarmSettings
            )
        }

        AnimatedVisibility(visible = settings.enabled) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FrequencySelector(settings.frequency, actions.onFrequencyChange)
                    TimeRows(settings, actions)
                    DaySelector(settings.days, actions.onDayToggle)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SwitchRow(
                    title = R.string.reminder_skip_when_reached,
                    description = R.string.reminder_skip_when_reached_desc,
                    checked = settings.skipWhenGoalReached,
                    onCheckedChange = actions.onSkipWhenGoalReachedChange
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(
                    onClick = actions.onOpenSoundSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reminder_sound_settings),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencySelector(selected: ReminderFrequency, onSelected: (ReminderFrequency) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.reminder_frequency),
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReminderFrequency.entries.forEach { frequency ->
                FilterChip(
                    selected = frequency == selected,
                    onClick = { onSelected(frequency) },
                    label = { Text(frequencyLabel(frequency)) }
                )
            }
        }
    }
}

@Composable
private fun TimeRows(settings: ReminderSettings, actions: ReminderActions) {
    val single = settings.frequency == ReminderFrequency.ONCE
    Column {
        TimeRow(
            label = if (single) R.string.reminder_time else R.string.reminder_from,
            time = settings.startTime,
            onTimeChange = actions.onStartTimeChange
        )
        if (!single) {
            TimeRow(
                label = R.string.reminder_until,
                time = settings.endTime,
                onTimeChange = actions.onEndTimeChange
            )
        }
    }
}

@Composable
private fun TimeRow(@StringRes label: Int, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = context.formatTime(time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
    if (showPicker) {
        TimePickerDialog(
            initial = time,
            onDismiss = { showPicker = false },
            onConfirm = {
                showPicker = false
                onTimeChange(it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(context)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        }
    )
}

@Composable
private fun DaySelector(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val locale = currentLocale()
    val days = remember(locale) { orderedDays(locale) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.reminder_days),
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                DayToggle(
                    label = day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                    contentDescription = day.getDisplayName(TextStyle.FULL_STANDALONE, locale),
                    selected = day in selected,
                    onClick = { onToggle(day) }
                )
            }
        }
    }
}

@Composable
private fun DayToggle(label: String, contentDescription: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SwitchRow(
    @StringRes title: Int,
    @StringRes description: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsWarning(@StringRes text: Int, isError: Boolean, onAction: () -> Unit) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAction) {
            Text(stringResource(R.string.settings_enable))
        }
    }
}

@Composable
private fun reminderSummary(settings: ReminderSettings): String {
    if (!settings.enabled) return stringResource(R.string.reminder_summary_off)
    if (settings.days.isEmpty()) return stringResource(R.string.reminder_summary_no_days)
    val context = LocalContext.current
    val locale = currentLocale()
    val times = when (settings.frequency) {
        ReminderFrequency.ONCE -> stringResource(R.string.reminder_summary_once, context.formatTime(settings.startTime))
        else -> stringResource(
            R.string.reminder_summary_interval,
            frequencyLabel(settings.frequency),
            context.formatTime(settings.startTime),
            context.formatTime(settings.endTime)
        )
    }
    val days = if (settings.days.size == DayOfWeek.entries.size) {
        stringResource(R.string.reminder_summary_every_day)
    } else {
        orderedDays(locale)
            .filter { it in settings.days }
            .joinToString(", ") { it.getDisplayName(TextStyle.SHORT_STANDALONE, locale) }
    }
    return "$times · $days"
}

@Composable
private fun frequencyLabel(frequency: ReminderFrequency): String {
    val minutes = frequency.intervalMinutes ?: return stringResource(R.string.reminder_frequency_once)
    val hours = minutes / MINUTES_PER_HOUR
    val rest = minutes % MINUTES_PER_HOUR
    return if (rest == 0) {
        stringResource(R.string.duration_hours, hours)
    } else {
        stringResource(R.string.duration_hours_minutes, hours, rest)
    }
}

@Composable
private fun currentLocale(): Locale =
    ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()

private fun orderedDays(locale: Locale): List<DayOfWeek> {
    val first = WeekFields.of(locale).firstDayOfWeek
    return List(DayOfWeek.entries.size) { first.plus(it.toLong()) }
}

private const val MINUTES_PER_HOUR = 60
