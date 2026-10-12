package com.lyf.androidmvp.feature.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.core.designsystem.component.AppEmptyView
import com.lyf.androidmvp.core.designsystem.component.AppErrorView
import com.lyf.androidmvp.core.designsystem.component.AppLoadingView
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.home.HomeUiState

/**
 * 首页主体内容区（状态分发：加载中 / 错误 / 空态 / 列表）
 */
@Composable
fun HomeBodyContent(
    state: HomeUiState,
    onRepoClick: (Repo) -> Unit,
    onFavoriteClick: (Repo) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp)
) {
    when {
        state.isLoading -> {
            AppLoadingView(
                message = "正在获取开源项目列表...",
                modifier = modifier
            )
        }
        state.errorMessage != null && state.repos.isEmpty() -> {
            AppErrorView(
                message = state.errorMessage ?: "加载失败",
                onRetry = onRetry,
                modifier = modifier
            )
        }
        state.filteredRepos.isEmpty() -> {
            AppEmptyView(
                message = if (state.searchQuery.isNotBlank()) "没有找到匹配的项目" else "暂无项目数据",
                onAction = onRefresh,
                actionLabel = "重新加载",
                modifier = modifier
            )
        }
        else -> {
            RepoListView(
                repos = state.filteredRepos,
                isRefreshing = state.isRefreshing,
                loadMoreState = state.loadMoreState,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onRetryLoadMore = onRetryLoadMore,
                onRepoClick = onRepoClick,
                onFavoriteClick = onFavoriteClick,
                listState = listState,
                contentPadding = contentPadding,
                modifier = modifier
            )
        }
    }
}
