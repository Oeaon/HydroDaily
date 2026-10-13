package com.oeaon.hydrodaily.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.model.UserProfile
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ProfileRepository {

    override val profile: Flow<UserProfile?> = dataStore.data
        .map { it.toProfile() }
        .distinctUntilChanged()

    override suspend fun save(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[WEIGHT] = profile.weightKg
            prefs[GENDER] = profile.gender.name
            prefs[ACTIVITY] = profile.activityLevel.name
            prefs[GOAL] = profile.dailyGoalMl
        }
    }

    private fun Preferences.toProfile(): UserProfile? {
        val weight = this[WEIGHT] ?: return null
        val gender = this[GENDER]?.let { enumValueOrNull<Gender>(it) } ?: return null
        val activity = this[ACTIVITY]?.let { enumValueOrNull<ActivityLevel>(it) } ?: return null
        val goal = this[GOAL] ?: return null
        return UserProfile(weight, gender, activity, goal)
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? =
        enumValues<T>().firstOrNull { it.name == name }

    private companion object {
        val WEIGHT = doublePreferencesKey("profile_weight_kg")
        val GENDER = stringPreferencesKey("profile_gender")
        val ACTIVITY = stringPreferencesKey("profile_activity_level")
        val GOAL = intPreferencesKey("profile_daily_goal_ml")
    }
}
