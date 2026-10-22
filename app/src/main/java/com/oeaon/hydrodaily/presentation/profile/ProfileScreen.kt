package com.oeaon.hydrodaily.presentation.profile

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.billing.PlayPremiumBilling
import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.premium.PremiumPlan
import com.oeaon.hydrodaily.presentation.common.formatVolume
import com.oeaon.hydrodaily.presentation.premium.PremiumCard
import com.oeaon.hydrodaily.presentation.premium.PremiumUiState
import com.oeaon.hydrodaily.presentation.premium.PremiumViewModel

@Composable
fun ProfileRoute(
    contentPadding: PaddingValues,
    privacyOptionsRequired: Boolean,
    onPrivacyOptions: () -> Unit,
    onProfileSaved: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val premiumState by premiumViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnProfileSaved by rememberUpdatedState(onProfileSaved)
    var notificationsBlocked by remember { mutableStateOf(false) }
    var exactAlarmBlocked by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        notificationsBlocked = !NotificationManagerCompat.from(context).areNotificationsEnabled()
        exactAlarmBlocked = !context.canScheduleExactAlarms()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.Saved -> currentOnProfileSaved()
            }
        }
    }

    ProfileScreen(
        state = state,
        premiumState = premiumState,
        contentPadding = contentPadding,
        notificationsBlocked = notificationsBlocked,
        exactAlarmBlocked = exactAlarmBlocked,
        privacyOptionsRequired = privacyOptionsRequired,
        onWeightChange = viewModel::onWeightChange,
        onGenderSelected = viewModel::onGenderSelected,
        onActivitySelected = viewModel::onActivityLevelSelected,
        onSave = viewModel::onSave,
        reminderActions = ReminderActions(
            onEnabledChange = viewModel::onRemindersToggled,
            onStartTimeChange = viewModel::onReminderStartTimeChange,
            onEndTimeChange = viewModel::onReminderEndTimeChange,
            onFrequencyChange = viewModel::onReminderFrequencyChange,
            onDayToggle = viewModel::onReminderDayToggled,
            onSkipWhenGoalReachedChange = viewModel::onSkipWhenGoalReachedToggled,
            onOpenSoundSettings = { context.openReminderChannelSettings() },
            onOpenNotificationSettings = { context.openNotificationSettings() },
            onOpenExactAlarmSettings = { context.openExactAlarmSettings() }
        ),
        onAnalyticsToggled = viewModel::onAnalyticsToggled,
        onPrivacyOptions = onPrivacyOptions,
        onBuyPremium = { plan -> activity?.let { premiumViewModel.onBuy(it, plan) } },
        onRestorePremium = premiumViewModel::onRestore,
        onManageSubscription = { context.openSubscriptionSettings() }
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    premiumState: PremiumUiState,
    contentPadding: PaddingValues,
    notificationsBlocked: Boolean,
    exactAlarmBlocked: Boolean,
    privacyOptionsRequired: Boolean,
    onWeightChange: (String) -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onActivitySelected: (ActivityLevel) -> Unit,
    onSave: () -> Unit,
    reminderActions: ReminderActions,
    onAnalyticsToggled: (Boolean) -> Unit,
    onPrivacyOptions: () -> Unit,
    onBuyPremium: (PremiumPlan) -> Unit,
    onRestorePremium: () -> Unit,
    onManageSubscription: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GoalCard(goalMl = state.savedGoalMl, visible = state.savedGoalMl != null && !state.isEditing)

        FormCard {
            OutlinedTextField(
                value = state.weightInput,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.profile_weight_label)) },
                suffix = { Text(stringResource(R.string.unit_kg)) },
                isError = state.weightError,
                supportingText = if (state.weightError) {
                    { Text(stringResource(R.string.profile_weight_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )

            ChoiceSection(
                title = R.string.profile_gender_label,
                options = Gender.entries,
                selected = state.gender,
                labelOf = ::genderLabel,
                isError = state.genderError,
                errorText = R.string.profile_gender_error,
                onSelected = onGenderSelected
            )

            ChoiceSection(
                title = R.string.profile_activity_label,
                options = ActivityLevel.entries,
                selected = state.activityLevel,
                labelOf = ::activityLabel,
                isError = state.activityError,
                errorText = R.string.profile_activity_error,
                onSelected = onActivitySelected
            )

            state.activityLevel?.let {
                Text(
                    text = stringResource(activityDescription(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.profile_calculate), style = MaterialTheme.typography.titleMedium)
            }
        }

        PremiumCard(
            state = premiumState,
            onBuy = onBuyPremium,
            onRestore = onRestorePremium,
            onManageSubscription = onManageSubscription
        )

        SectionTitle(R.string.reminders_title) {
            ReminderSettingsCard(
                settings = state.reminders,
                notificationsBlocked = notificationsBlocked,
                exactAlarmBlocked = exactAlarmBlocked,
                actions = reminderActions
            )
        }

        SectionTitle(R.string.settings_title) {
            SettingsCard(
                analyticsEnabled = state.analyticsEnabled,
                privacyOptionsRequired = privacyOptionsRequired,
                onAnalyticsToggled = onAnalyticsToggled,
                onPrivacyOptions = onPrivacyOptions
            )
        }

        Text(
            text = stringResource(R.string.profile_goal_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GoalCard(goalMl: Int?, visible: Boolean) {
    val context = LocalContext.current
    AnimatedVisibility(visible = visible && goalMl != null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.profile_goal_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = context.formatVolume(goalMl ?: 0),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceSection(
    @StringRes title: Int,
    options: List<T>,
    selected: T?,
    labelOf: (T) -> Int,
    isError: Boolean,
    @StringRes errorText: Int,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size)
                ) {
                    Text(stringResource(labelOf(option)), maxLines = 1)
                }
            }
        }
        if (isError) {
            Text(
                text = stringResource(errorText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    analyticsEnabled: Boolean,
    privacyOptionsRequired: Boolean,
    onAnalyticsToggled: (Boolean) -> Unit,
    onPrivacyOptions: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        SwitchRow(
            title = R.string.settings_analytics,
            description = R.string.settings_analytics_desc,
            checked = analyticsEnabled,
            onCheckedChange = onAnalyticsToggled
        )
        if (privacyOptionsRequired) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TextButton(
                onClick = onPrivacyOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_privacy),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        val context = LocalContext.current
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TextButton(
            onClick = { context.openPrivacyPolicy() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_privacy_policy),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@StringRes
private fun genderLabel(gender: Gender): Int = when (gender) {
    Gender.FEMALE -> R.string.gender_female
    Gender.MALE -> R.string.gender_male
}

@StringRes
private fun activityLabel(level: ActivityLevel): Int = when (level) {
    ActivityLevel.LOW -> R.string.activity_low
    ActivityLevel.MODERATE -> R.string.activity_moderate
    ActivityLevel.HIGH -> R.string.activity_high
}

@StringRes
private fun activityDescription(level: ActivityLevel): Int = when (level) {
    ActivityLevel.LOW -> R.string.activity_low_desc
    ActivityLevel.MODERATE -> R.string.activity_moderate_desc
    ActivityLevel.HIGH -> R.string.activity_high_desc
}

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun Context.openReminderChannelSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        openNotificationSettings()
        return
    }
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .putExtra(Settings.EXTRA_CHANNEL_ID, getString(R.string.reminder_channel_id))
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun Context.openSubscriptionSettings() {
    val uri = "https://play.google.com/store/account/subscriptions?sku=${PlayPremiumBilling.MONTHLY_PRODUCT_ID}&package=$packageName"
        .toUri()
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private fun Context.openPrivacyPolicy() {
    val uri = "https://oeaon.github.io/hydrodaily/privacy.html".toUri()
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private fun Context.canScheduleExactAlarms(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.fromParts("package", packageName, null))
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
