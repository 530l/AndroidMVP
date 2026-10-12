package com.lyf.androidmvp.feature.main

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lyf.androidmvp.navigation.AppDestination
import com.lyf.androidmvp.feature.detail.DetailResult

/**
 * Main 模块专属解耦路由注册器
 */
fun EntryProviderScope<NavKey>.mainNavEntry(
    onNavigateToDetail: (Long, String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    detailResultProvider: () -> DetailResult?,
    onDetailResultConsumed: (DetailResult) -> Unit
) {
    entry<AppDestination.Main> {
        MainScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToSettings = onNavigateToSettings,
            detailResult = detailResultProvider(),
            onDetailResultConsumed = onDetailResultConsumed
        )
    }
}
