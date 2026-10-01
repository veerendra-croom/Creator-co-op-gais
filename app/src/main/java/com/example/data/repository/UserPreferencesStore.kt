package com.example.data.repository

import com.example.data.local.UserSettingsDao
import com.example.data.model.UserSetting

class UserPreferencesStore(private val userSettingsDao: UserSettingsDao) {
    suspend fun saveVerificationBadgeRequest(userId: String, isRequested: Boolean) {
        val setting = UserSetting(
            id = "${userId}_verification_badge_requested",
            userId = userId,
            key = "verification_badge_requested",
            value = isRequested.toString()
        )
        userSettingsDao.setSetting(setting)
    }

    suspend fun isVerificationBadgeRequested(userId: String): Boolean {
        val value = userSettingsDao.getSetting(userId, "verification_badge_requested")
        return value == "true"
    }
}
