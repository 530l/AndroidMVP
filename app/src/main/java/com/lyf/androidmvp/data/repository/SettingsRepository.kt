package com.lyf.androidmvp.data.repository

import com.lyf.androidmvp.core.datastore.AppPreferences
import com.lyf.androidmvp.core.datastore.AppThemeConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(private val preferences: AppPreferences) {
    val themeConfig get() = preferences.themeConfig
    val authToken get() = preferences.authToken
    val username get() = preferences.username

    suspend fun setThemeConfig(theme: AppThemeConfig) = preferences.setThemeConfig(theme)

    suspend fun saveUser(token: String, username: String) {
        preferences.setAuthToken(token)
        preferences.setUsername(username)
    }

    suspend fun logout() = preferences.clear()
}
