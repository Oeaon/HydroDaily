package com.oeaon.hydrodaily.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.usecase.AddWaterUseCase
import com.oeaon.hydrodaily.domain.usecase.ObserveHydrationStatusUseCase
import com.oeaon.hydrodaily.domain.usecase.UndoLastIntakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    observeHydrationStatus: ObserveHydrationStatusUseCase,
    private val addWater: AddWaterUseCase,
    private val undoLastIntake: UndoLastIntakeUseCase
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = observeHydrationStatus()
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TodayUiState())

    fun onAddWater(amountMl: Int) {
        viewModelScope.launch { addWater(amountMl) }
    }

    fun onUndo() {
        viewModelScope.launch { undoLastIntake() }
    }

    private fun HydrationStatus.toUiState() = TodayUiState(
        isLoading = false,
        hasProfile = profile != null,
        consumedMl = intake.totalMl,
        goalMl = goalMl,
        remainingMl = remainingMl,
        progress = progress,
        isGoalReached = isGoalReached,
        entriesCount = intake.entriesMl.size
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

data class TodayUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val consumedMl: Int = 0,
    val goalMl: Int = 0,
    val remainingMl: Int = 0,
    val progress: Float = 0f,
    val isGoalReached: Boolean = false,
    val entriesCount: Int = 0
) {
    val canUndo: Boolean get() = entriesCount > 0
    val percent: Int get() = if (goalMl == 0) 0 else (consumedMl * 100 / goalMl)
}
