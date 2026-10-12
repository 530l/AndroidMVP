package com.lyf.androidmvp.feature.detail

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lyf.androidmvp.navigation.AppDestination

fun EntryProviderScope<NavKey>.detailNavEntry(
    onNavigateBack: (AppDestination.Detail, DetailResult?) -> Unit,
    onRegisterBackHandler: (AppDestination.Detail, (() -> Unit)?) -> Unit
) {
    entry<AppDestination.Detail> { destination ->
        DetailScreen(
            repoId = destination.repoId,
            repoName = destination.repoName,
            fromSource = destination.fromSource,
            onNavigateBack = { result -> onNavigateBack(destination, result) },
            onRegisterBackHandler = { handler -> onRegisterBackHandler(destination, handler) }
        )
    }
}
