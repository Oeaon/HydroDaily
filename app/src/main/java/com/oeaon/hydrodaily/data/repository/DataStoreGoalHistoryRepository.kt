package com.oeaon.hydrodaily.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.oeaon.hydrodaily.domain.repository.GoalHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreGoalHistoryRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : GoalHistoryRepository {

    override val goalDays: Flow<Set<LocalDate>> = dataStore.data
        .map { it.goalDays() }
        .distinctUntilChanged()

    override suspend fun setGoalReached(date: LocalDate, reached: Boolean) {
        dataStore.edit { prefs ->
            val days = prefs.goalDays().let { if (reached) it + date else it - date }
            val oldestKept = date.minusDays(MAX_DAYS_KEPT)
            prefs[GOAL_DAYS] = days.filterNot { it.isBefore(oldestKept) }.map { it.toString() }.toSet()
        }
    }

    private fun Preferences.goalDays(): Set<LocalDate> =
        this[GOAL_DAYS].orEmpty().mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()

    private companion object {
        val GOAL_DAYS = stringSetPreferencesKey("goal_reached_days")
        const val MAX_DAYS_KEPT = 400L
    }
}
