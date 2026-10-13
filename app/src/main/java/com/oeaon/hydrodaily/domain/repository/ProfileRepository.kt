package com.oeaon.hydrodaily.domain.repository

import com.oeaon.hydrodaily.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<UserProfile?>
    suspend fun save(profile: UserProfile)
}
