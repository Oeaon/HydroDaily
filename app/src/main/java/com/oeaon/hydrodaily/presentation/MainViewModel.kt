package com.oeaon.hydrodaily.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import com.oeaon.hydrodaily.presentation.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Destination?>(null)
    val startDestination: StateFlow<Destination?> = _startDestination.asStateFlow()

    val notificationPermissionAsked: StateFlow<Boolean> = settingsRepository.notificationPermissionAsked
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        viewModelScope.launch {
            val hasProfile = profileRepository.profile.first() != null
            _startDestination.value = if (hasProfile) Destination.Today else Destination.Profile
        }
    }

    fun onNotificationPermissionRequested() {
        viewModelScope.launch { settingsRepository.setNotificationPermissionAsked() }
    }
}
