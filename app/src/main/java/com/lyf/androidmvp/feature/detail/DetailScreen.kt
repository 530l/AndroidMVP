package com.lyf.androidmvp.feature.detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.androidmvp.core.designsystem.component.AppToastMessages
import com.lyf.androidmvp.core.designsystem.component.appFrostedGlass
import com.lyf.androidmvp.core.designsystem.component.appHazeSource
import com.lyf.androidmvp.core.designsystem.component.rememberAppHazeState

/**
 * 详情页容器组件 (Stateful)
 * 职责：负责接收路由入参、初始化 ViewModel、观察保存完成状态并委托给 DetailContent。
 */
@Composable
fun DetailScreen(
    repoId: Long,
    repoName: String,
    fromSource: String = "首页",
    onNavigateBack: (DetailResult?) -> Unit,
    onRegisterBackHandler: ((() -> Unit)?) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val navigateBack by rememberUpdatedState(onNavigateBack)
    val handleBackWithSave = remember(viewModel) {
        {
            val result = viewModel.saveAndGetResult(forceResult = false)
            navigateBack(result)
        }
    }
    val handleSaveAndFinish = remember(viewModel) {
        {
            val result = viewModel.saveAndGetResult(forceResult = true)
            navigateBack(result)
        }
    }

    DisposableEffect(repoId, onRegisterBackHandler) {
        onRegisterBackHandler(handleBackWithSave)
        onDispose { onRegisterBackHandler(null) }
    }

    LaunchedEffect(repoId, repoName, fromSource) {
        viewModel.initialize(repoId, repoName, fromSource)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppToastMessages(state.messages, viewModel::dismissMessage)

    DetailContent(
        repoName = repoName,
        fromSource = fromSource,
        state = state,
        onBackClick = handleBackWithSave,
        onToggleFavorite = { viewModel.onEvent(DetailUiEvent.ToggleFavorite) },
        onUpdateRemark = { viewModel.onEvent(DetailUiEvent.UpdateRemark(it)) },
        onReturnResult = handleSaveAndFinish,
        onNavigateBack = handleBackWithSave
    )
}

/**
 * 详情页无状态内容组件 (Stateless)
 * 职责：平铺各子模块卡片，消除金字塔嵌套，支持独立预览与测试。
 */
@Composable
fun DetailContent(
    repoName: String,
    fromSource: String,
    state: DetailUiState,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onUpdateRemark: (String) -> Unit,
    onReturnResult: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = rememberAppHazeState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DetailTopBar(
                title = repoName,
                isFavorite = state.repo?.isFavorite ?: false,
                showFavoriteAction = state.repo != null,
                onBackClick = onBackClick,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.appFrostedGlass(
                    state = hazeState,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    blurRadius = 24.dp
                )
            )
        }
    ) { innerPadding ->
        DetailBodyContent(
            state = state,
            fromSource = fromSource,
            onNavigateBack = onNavigateBack,
            onUpdateRemark = onUpdateRemark,
            onToggleFavorite = onToggleFavorite,
            onReturnResult = onReturnResult,
            modifier = Modifier
                .fillMaxSize()
                .appHazeSource(hazeState)
                .padding(top = innerPadding.calculateTopPadding())
        )
    }
}
