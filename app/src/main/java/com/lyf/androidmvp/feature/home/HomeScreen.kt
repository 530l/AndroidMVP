package com.lyf.androidmvp.feature.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.androidmvp.core.designsystem.component.AppToast
import com.lyf.androidmvp.core.designsystem.component.AppToastMessages
import com.lyf.androidmvp.core.designsystem.component.CollapsibleStickyTabPager
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.detail.DetailResult
import com.lyf.androidmvp.feature.home.component.ExploreFloatingActionButton
import com.lyf.androidmvp.feature.home.component.HomeHeaderSection
import com.lyf.androidmvp.feature.home.component.HomePagerSection
import com.lyf.androidmvp.feature.home.component.HomeStickyTabs

/**
 * 首页容器组件 (Stateful)
 * 职责：负责与 ViewModel 绑定、收集 UI 状态、通过 Toaster 消费提示消息，并将纯状态委托给 HomeContent。
 */
@Composable
fun HomeScreen(
    onNavigateToDetail: (Long, String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    detailResult: DetailResult? = null,
    onDetailResultConsumed: (DetailResult) -> Unit = {},
    isActive: Boolean = true,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppToastMessages(state.messages, viewModel::dismissMessage)
    LaunchedEffect(detailResult?.resultId) {
        detailResult?.let {
            viewModel.acceptDetailResult(it)
            onDetailResultConsumed(it)
        }
    }

    HomeContent(
        state = state,
        onRefresh = { viewModel.onEvent(HomeUiEvent.Refresh) },
        onLoadMore = { viewModel.onEvent(HomeUiEvent.LoadMore) },
        onRetryLoadMore = { viewModel.onEvent(HomeUiEvent.RetryLoadMore) },
        onSettingsClick = onNavigateToSettings,
        onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
        onRepoClick = { onNavigateToDetail(it.id, it.name, "发现精选瀑布流") },
        onFavoriteClick = { viewModel.onEvent(HomeUiEvent.ToggleFavorite(it)) },
        onRetry = { viewModel.onEvent(HomeUiEvent.LoadData) }
    )
}

/**
 * 首页无状态内容组件 (Stateless)
 * 职责：消除一切金字塔式嵌套，仅以高度语义化的独立区块完成折叠吸顶联动组装。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRepoClick: (Repo) -> Unit,
    onFavoriteClick: (Repo) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val repoListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            CollapsibleStickyTabPager(
                modifier = Modifier.fillMaxSize(),
                headerContent = { collapseFraction ->
                    HomeHeaderSection(
                        state = state,
                        collapseFraction = collapseFraction,
                        onRefresh = onRefresh,
                        onSettingsClick = onSettingsClick,
                        onSearchQueryChange = onSearchQueryChange,
                        onBannerClick = { banner ->
                            AppToast.show(banner.title)
                        },
                        onMenuItemClick = { title ->
                            AppToast.show("进入专区：$title")
                        }
                    )
                },
                stickyTabContent = {
                    HomeStickyTabs(pagerState = pagerState)
                },
                pagerContent = {
                    HomePagerSection(
                        pagerState = pagerState,
                        state = state,
                        onRepoClick = onRepoClick,
                        onFavoriteClick = onFavoriteClick,
                        onRetry = onRetry,
                        onRefresh = onRefresh,
                        onLoadMore = onLoadMore,
                        onRetryLoadMore = onRetryLoadMore,
                        listState = repoListState
                    )
                },
                floatingActionButton = {
                    ExploreFloatingActionButton(
                        onClick = {
                            AppToast.show("已开启AI灵感创作模式 ✨")
                        }
                    )
                }
            )
        }
    }
}
