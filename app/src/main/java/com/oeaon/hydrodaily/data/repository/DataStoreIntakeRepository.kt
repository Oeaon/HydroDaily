package com.oeaon.hydrodaily.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.oeaon.hydrodaily.domain.model.DailyIntake
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreIntakeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : IntakeRepository {

    override fun observe(date: LocalDate): Flow<DailyIntake> =
        dataStore.data
            .map { DailyIntake(date, it.entriesFor(date)) }
            .distinctUntilChanged()

    override suspend fun add(date: LocalDate, amountMl: Int) {
        dataStore.edit { prefs -> prefs.writeEntries(date, prefs.entriesFor(date) + amountMl) }
    }

    override suspend fun removeLast(date: LocalDate) {
        dataStore.edit { prefs -> prefs.writeEntries(date, prefs.entriesFor(date).dropLast(1)) }
    }

    private fun Preferences.entriesFor(date: LocalDate): List<Int> {
        if (this[DATE] != date.toString()) return emptyList()
        return this[ENTRIES].orEmpty()
            .split(SEPARATOR)
            .mapNotNull { it.toIntOrNull() }
    }

    private fun MutablePreferences.writeEntries(date: LocalDate, entries: List<Int>) {
        this[DATE] = date.toString()
        this[ENTRIES] = entries.joinToString(SEPARATOR)
    }

    private companion object {
        val DATE = stringPreferencesKey("intake_date")
        val ENTRIES = stringPreferencesKey("intake_entries_ml")
        const val SEPARATOR = ","
    }
}
