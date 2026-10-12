package com.lyf.androidmvp.feature.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.core.designsystem.component.paging.AppPagingLazyColumn
import com.lyf.androidmvp.core.designsystem.component.paging.LoadMoreState
import com.lyf.androidmvp.domain.model.Repo

/**
 * 列表视图组件 (接入 Material 3 下拉刷新 + 无感加载更多)
 */
@Composable
fun RepoListView(
    repos: List<Repo>,
    isRefreshing: Boolean,
    loadMoreState: LoadMoreState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onRepoClick: (Repo) -> Unit,
    onFavoriteClick: (Repo) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    isPullToRefreshEnabled: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp),
    modifier: Modifier = Modifier
) {
    AppPagingLazyColumn(
        state = listState,
        isRefreshing = isRefreshing,
        loadMoreState = loadMoreState,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onRetryLoadMore = onRetryLoadMore,
        isPullToRefreshEnabled = isPullToRefreshEnabled,
        contentPadding = contentPadding,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = repos,
            key = { it.id }
        ) { repo ->
            RepoItemCard(
                repo = repo,
                onClick = { onRepoClick(repo) },
                onFavoriteClick = { onFavoriteClick(repo) }
            )
        }
    }
}
