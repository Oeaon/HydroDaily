package com.oeaon.hydrodaily.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oeaon.hydrodaily.domain.model.HydrationStatus
import com.oeaon.hydrodaily.domain.model.Streak
import com.oeaon.hydrodaily.domain.usecase.AddWaterUseCase
import com.oeaon.hydrodaily.domain.usecase.ClaimReviewPromptUseCase
import com.oeaon.hydrodaily.domain.usecase.ObserveHydrationStatusUseCase
import com.oeaon.hydrodaily.domain.usecase.ObserveStreakUseCase
import com.oeaon.hydrodaily.domain.usecase.UndoLastIntakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    observeHydrationStatus: ObserveHydrationStatusUseCase,
    observeStreak: ObserveStreakUseCase,
    private val addWater: AddWaterUseCase,
    private val undoLastIntake: UndoLastIntakeUseCase,
    private val claimReviewPrompt: ClaimReviewPromptUseCase
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = combine(observeHydrationStatus(), observeStreak()) { status, streak ->
        status.toUiState(streak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TodayUiState())

    private val _events = Channel<TodayEvent>(Channel.BUFFERED)
    val events: Flow<TodayEvent> = _events.receiveAsFlow()

    fun onAddWater(amountMl: Int) {
        viewModelScope.launch {
            val result = addWater(amountMl)
            if (result.goalJustReached && claimReviewPrompt(result.streakDays)) {
                _events.send(TodayEvent.RequestReview)
            }
        }
    }

    fun onUndo() {
        viewModelScope.launch { undoLastIntake() }
    }

    private fun HydrationStatus.toUiState(streak: Streak) = TodayUiState(
        isLoading = false,
        hasProfile = profile != null,
        consumedMl = intake.totalMl,
        goalMl = goalMl,
        remainingMl = remainingMl,
        progress = progress,
        isGoalReached = isGoalReached,
        entriesCount = intake.entriesMl.size,
        currentStreak = streak.current,
        bestStreak = streak.best,
        streakCompletedToday = streak.completedToday
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
    val entriesCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val streakCompletedToday: Boolean = false
) {
    val canUndo: Boolean get() = entriesCount > 0
    val percent: Int get() = if (goalMl == 0) 0 else (consumedMl * 100 / goalMl)
}

sealed interface TodayEvent {
    data object RequestReview : TodayEvent
}
