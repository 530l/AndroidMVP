package com.lyf.androidmvp.feature.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.androidmvp.core.datastore.AppThemeConfig
import com.lyf.androidmvp.core.designsystem.component.AppToastMessages
import com.lyf.androidmvp.core.designsystem.component.AppTopBar
import com.lyf.androidmvp.core.designsystem.component.appFrostedGlass
import com.lyf.androidmvp.core.designsystem.component.appHazeSource
import com.lyf.androidmvp.core.designsystem.component.rememberAppHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

/**
 * 设置页容器组件 (Stateful)
 * 职责：绑定 ViewModel、收集设置状态、通过 Toaster 消费提示消息。
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppToastMessages(state.messages, viewModel::dismissMessage)

    SettingsContent(
        state = state,
        onBackClick = onNavigateBack,
        onThemeChange = { viewModel.onEvent(SettingsUiEvent.ChangeTheme(it)) },
        onSaveToken = { token, username ->
            viewModel.onEvent(SettingsUiEvent.SaveUserToken(token, username))
        },
        onLogout = { viewModel.onEvent(SettingsUiEvent.Logout) },
        onClearCache = { viewModel.onEvent(SettingsUiEvent.ClearCache) },
        onShowMessage = { viewModel.onEvent(SettingsUiEvent.ShowMessage(it)) }
    )
}

/**
 * 设置页无状态内容组件 (Stateless)
 * 职责：平铺各设置功能卡片，消除金字塔嵌套，集成 Haze 实时磨砂毛玻璃顶栏。
 */
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBackClick: () -> Unit,
    onThemeChange: (AppThemeConfig) -> Unit,
    onSaveToken: (String, String) -> Unit,
    onLogout: () -> Unit,
    onClearCache: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val miuixColors = if (isDark) miuixDarkColorScheme() else miuixLightColorScheme()

    MiuixTheme(colors = miuixColors) {
        // 实例化 Haze 采样控制器
        val hazeState = rememberAppHazeState()

        Scaffold(
            modifier = modifier,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            topBar = {
                AppTopBar(
                    title = "系统设置",
                    canNavigateBack = true,
                    onNavigateBack = onBackClick,
                    modifier = Modifier.appFrostedGlass(
                        state = hazeState,
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                        blurRadius = 24.dp
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .appHazeSource(hazeState) // 注册为 Haze 实时毛玻璃采样源
                    .verticalScroll(rememberScrollState())
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 顶部个人账户与凭据卡片
                AccountSettingsCard(
                    username = state.username,
                    token = state.token,
                    onSaveToken = onSaveToken,
                    onLogout = onLogout
                )

                // 2. 主题与显示
                ThemeSettingsCard(
                    themeConfig = state.themeConfig,
                    onThemeChange = onThemeChange
                )

                // 3. 系统权限与隐私
                PermissionSettingsCard(
                    onShowMessage = onShowMessage
                )

                // 4. 本地存储与离线缓存
                CacheSettingsCard(
                    onClearCache = onClearCache
                )

                // 5. 关于与技术体系
                AboutSettingsCard(
                    appVersion = state.appVersion
                )

                // 底部轻量品牌标识
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WanAndroid MVP Architecture",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Jetpack Compose · Miuix · Haze · Clean MVI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
