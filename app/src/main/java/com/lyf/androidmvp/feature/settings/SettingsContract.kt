package com.lyf.androidmvp.feature.settings

import com.lyf.androidmvp.core.datastore.AppThemeConfig
import com.lyf.androidmvp.core.common.UiMessage
import com.lyf.androidmvp.feature.base.UiEvent
import com.lyf.androidmvp.feature.base.UiState

data class SettingsUiState(
    val themeConfig: AppThemeConfig = AppThemeConfig.FOLLOW_SYSTEM,
    val token: String? = null,
    val username: String? = null,
    val appVersion: String = "1.0.0",
    override val messages: List<UiMessage> = emptyList()
) : UiState

sealed interface SettingsUiEvent : UiEvent {
    data class ChangeTheme(val theme: AppThemeConfig) : SettingsUiEvent
    data class SaveUserToken(val token: String, val username: String) : SettingsUiEvent
    data object ClearCache : SettingsUiEvent
    data object Logout : SettingsUiEvent
    data class ShowMessage(val message: String) : SettingsUiEvent
}
