package com.lyf.androidmvp.feature.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lyf.androidmvp.navigation.AppDestination

/**
 * Settings 模块专属解耦路由注册器
 */
fun EntryProviderScope<NavKey>.settingsNavEntry(
    onNavigateBack: () -> Unit
) {
    entry<AppDestination.Settings> {
        SettingsScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
