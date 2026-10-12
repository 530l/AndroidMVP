package com.lyf.androidmvp.feature.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.home.HomeUiState

/**
 * 首页横向分页内容区 (解耦 HorizontalPager 内部嵌套)
 */
@Composable
fun HomePagerSection(
    pagerState: PagerState,
    state: HomeUiState,
    onRepoClick: (Repo) -> Unit,
    onFavoriteClick: (Repo) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> HomeBodyContent(
                state = state,
                onRepoClick = onRepoClick,
                onFavoriteClick = onFavoriteClick,
                onRetry = onRetry,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onRetryLoadMore = onRetryLoadMore,
                listState = listState,
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            )
            1 -> ExploreHotFeedTab(
                onRepoClick = onRepoClick,
                modifier = Modifier.fillMaxSize()
            )
            2 -> ExploreOfficialFeedTab(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
