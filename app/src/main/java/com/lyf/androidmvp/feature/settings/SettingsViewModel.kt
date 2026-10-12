package com.lyf.androidmvp.feature.settings

import androidx.lifecycle.viewModelScope
import com.lyf.androidmvp.BuildConfig
import com.lyf.androidmvp.core.datastore.AppThemeConfig
import com.lyf.androidmvp.data.repository.SettingsRepository
import com.lyf.androidmvp.domain.repository.RepoRepository
import com.lyf.androidmvp.feature.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val repoRepository: RepoRepository
) : BaseViewModel<SettingsUiState, SettingsUiEvent>(
    SettingsUiState(appVersion = BuildConfig.VERSION_NAME),
    { state, messages -> state.copy(messages = messages) }
) {
    init {
        viewModelScope.launch {
            combine(
                settingsRepository.themeConfig,
                settingsRepository.authToken,
                settingsRepository.username
            ) { theme, token, username -> Triple(theme, token, username) }
                .catch { showMessage(it.localizedMessage ?: "读取设置失败") }
                .collect { (theme, token, username) ->
                    updateState { it.copy(themeConfig = theme, token = token, username = username) }
                }
        }
    }

    override fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ChangeTheme -> changeTheme(event.theme)
            is SettingsUiEvent.SaveUserToken -> launchAction {
                settingsRepository.saveUser(event.token, event.username)
                showMessage("用户配置已保存")
            }
            SettingsUiEvent.ClearCache -> launchAction {
                repoRepository.clearCache()
                showMessage("项目缓存已清空，收藏和备注已保留")
            }
            SettingsUiEvent.Logout -> launchAction {
                settingsRepository.logout()
                showMessage("已退出登录")
            }
            is SettingsUiEvent.ShowMessage -> showMessage(event.message)
        }
    }

    private fun changeTheme(theme: AppThemeConfig) = launchAction {
        settingsRepository.setThemeConfig(theme)
        showMessage("主题已切换")
    }
}
