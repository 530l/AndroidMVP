package com.lyf.androidmvp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.lyf.androidmvp.feature.detail.DetailResult
import com.lyf.androidmvp.feature.detail.detailNavEntry
import com.lyf.androidmvp.feature.main.mainNavEntry
import com.lyf.androidmvp.feature.settings.settingsNavEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 导航宿主保存待消费回参；系统返回由 NavDisplay 处理手势并转交详情保存流程。
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(AppDestination.Main)
) {
    val saveableStateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators = remember(saveableStateHolderDecorator, viewModelStoreDecorator) {
        listOf(saveableStateHolderDecorator, viewModelStoreDecorator)
    }
    val detailBackHandlers = remember { mutableMapOf<NavKey, () -> Unit>() }
    // 回参属于当前导航宿主，接收方确认前保留，并支持系统保存状态恢复。
    var pendingResults by rememberSaveable { mutableStateOf(listOf<String>()) }

    val memoizedEntryProvider = remember(backStack) {
        entryProvider {
            mainNavEntry(
                onNavigateToDetail = { id, name, fromSource ->
                    val target = AppDestination.Detail(id, name, fromSource)
                    if (backStack.lastOrNull() != target) backStack.add(target)
                },
                onNavigateToSettings = {
                    if (backStack.lastOrNull() != AppDestination.Settings) {
                        backStack.add(AppDestination.Settings)
                    }
                },
                detailResultProvider = {
                    pendingResults.firstOrNull()?.let { Json.decodeFromString<DetailResult>(it) }
                },
                onDetailResultConsumed = { result ->
                    pendingResults = pendingResults.filterNot {
                        Json.decodeFromString<DetailResult>(it).resultId == result.resultId
                    }
                }
            )
            detailNavEntry(
                onNavigateBack = { destination, result ->
                    // 页面转场期间的旧回调不能移除新页面或根入口。
                    if (backStack.size > 1 && backStack.lastOrNull() == destination) {
                        if (result != null) pendingResults = pendingResults + Json.encodeToString(result)
                        backStack.removeLastOrNull()
                    }
                },
                onRegisterBackHandler = { destination, handler ->
                    if (handler == null) detailBackHandlers.remove(destination)
                    else detailBackHandlers[destination] = handler
                }
            )
            settingsNavEntry(
                onNavigateBack = {
                    if (backStack.size > 1 && backStack.lastOrNull() == AppDestination.Settings) {
                        backStack.removeLastOrNull()
                    }
                }
            )
        }
    }

    val pushTransitionSpec = remember { iosPushTransitionSpec<NavKey>() }
    val popTransitionSpec = remember { iosPopTransitionSpec<NavKey>() }
    val predictivePopTransitionSpec = remember { iosPredictivePopTransitionSpec<NavKey>() }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            val destination = backStack.lastOrNull()
            if (destination is AppDestination.Detail) {
                // 保存完成后由详情 UI 退栈，取消预测性手势不会触发此分支。
                detailBackHandlers[destination]?.invoke()
            } else if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = entryDecorators,
        entryProvider = memoizedEntryProvider,
        transitionSpec = pushTransitionSpec,
        popTransitionSpec = popTransitionSpec,
        predictivePopTransitionSpec = predictivePopTransitionSpec
    )
}
