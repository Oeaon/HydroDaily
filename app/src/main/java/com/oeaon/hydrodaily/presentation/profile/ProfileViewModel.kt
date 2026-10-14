package com.oeaon.hydrodaily.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.model.ReminderFrequency
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.domain.usecase.SaveProfileUseCase
import com.oeaon.hydrodaily.domain.usecase.SetAnalyticsEnabledUseCase
import com.oeaon.hydrodaily.domain.usecase.UpdateReminderSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    private val saveProfile: SaveProfileUseCase,
    private val updateReminderSettings: UpdateReminderSettingsUseCase,
    private val setAnalyticsEnabled: SetAnalyticsEnabledUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.profile.first()
            _uiState.update { state ->
                if (profile == null) state else state.copy(
                    weightInput = formatWeight(profile.weightKg),
                    gender = profile.gender,
                    activityLevel = profile.activityLevel,
                    savedGoalMl = profile.dailyGoalMl,
                    isEditing = false
                )
            }
        }
        settingsRepository.reminderSettings
            .onEach { settings -> _uiState.update { it.copy(reminders = settings) } }
            .launchIn(viewModelScope)
        settingsRepository.analyticsEnabled
            .onEach { enabled -> _uiState.update { it.copy(analyticsEnabled = enabled) } }
            .launchIn(viewModelScope)
    }

    fun onWeightChange(input: String) {
        val sanitized = input.replace(',', '.').filter { it.isDigit() || it == '.' }.take(MAX_WEIGHT_CHARS)
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(weightInput = sanitized, weightError = false, isEditing = true) }
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { it.copy(gender = gender, genderError = false, isEditing = true) }
    }

    fun onActivityLevelSelected(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level, activityError = false, isEditing = true) }
    }

    fun onSave() {
        val state = _uiState.value
        viewModelScope.launch {
            when (val result = saveProfile(state.weightInput.toDoubleOrNull(), state.gender, state.activityLevel)) {
                is SaveProfileUseCase.Result.Saved -> {
                    _uiState.update { it.copy(savedGoalMl = result.profile.dailyGoalMl, isEditing = false) }
                    _events.send(ProfileEvent.Saved)
                }
                SaveProfileUseCase.Result.InvalidWeight -> _uiState.update { it.copy(weightError = true) }
                SaveProfileUseCase.Result.MissingGender -> _uiState.update { it.copy(genderError = true) }
                SaveProfileUseCase.Result.MissingActivityLevel -> _uiState.update { it.copy(activityError = true) }
            }
        }
    }

    fun onRemindersToggled(enabled: Boolean) = updateReminders { it.copy(enabled = enabled) }

    fun onReminderStartTimeChange(time: LocalTime) = updateReminders { it.copy(startTime = time) }

    fun onReminderEndTimeChange(time: LocalTime) = updateReminders { it.copy(endTime = time) }

    fun onReminderFrequencyChange(frequency: ReminderFrequency) = updateReminders { it.copy(frequency = frequency) }

    fun onReminderDayToggled(day: DayOfWeek) = updateReminders { settings ->
        settings.copy(days = if (day in settings.days) settings.days - day else settings.days + day)
    }

    fun onSkipWhenGoalReachedToggled(skip: Boolean) = updateReminders { it.copy(skipWhenGoalReached = skip) }

    fun onAnalyticsToggled(enabled: Boolean) {
        viewModelScope.launch { setAnalyticsEnabled(enabled) }
    }

    private fun updateReminders(transform: (ReminderSettings) -> ReminderSettings) {
        viewModelScope.launch { updateReminderSettings(transform) }
    }

    private fun formatWeight(weight: Double): String =
        if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()

    private companion object {
        const val MAX_WEIGHT_CHARS = 5
    }
}

data class ProfileUiState(
    val weightInput: String = "",
    val gender: Gender? = null,
    val activityLevel: ActivityLevel? = null,
    val weightError: Boolean = false,
    val genderError: Boolean = false,
    val activityError: Boolean = false,
    val savedGoalMl: Int? = null,
    val isEditing: Boolean = true,
    val reminders: ReminderSettings = ReminderSettings(),
    val analyticsEnabled: Boolean = true
)

sealed interface ProfileEvent {
    data object Saved : ProfileEvent
}
