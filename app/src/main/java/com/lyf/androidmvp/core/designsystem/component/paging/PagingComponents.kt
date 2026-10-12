package com.lyf.androidmvp.core.designsystem.component.paging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * 分页加载状态枚举契约 (完全解耦于具体业务)
 */
sealed interface LoadMoreState {
    data object Idle : LoadMoreState                        // 闲置/正常状态
    data object Loading : LoadMoreState                     // 正在加载下一页
    data object NoMore : LoadMoreState                      // 全部数据加载完毕
    data class Error(val message: String) : LoadMoreState   // 下一页加载失败（支持点击重试）
}

/**
 * LazyColumn 普通列表触底预加载监听扩展
 *
 * 核心机制优化：
 * 1. 响应式联动状态机：同时监听滚动位置与加载状态。当加载完成后若用户仍处于预加载区，自动触发下一页，彻底消除“需要下滑再上滑才触发”的死锁问题。
 * 2. 预加载缓冲（buffer = 3）：提前倒数第 N 项（默认 3 项）在后台静默发起网络请求，网络返回时列表已无缝扩充，杜绝“触底卡顿一会儿才加载”的阻滞感。
 *
 * @param loadMoreState 当前加载更多状态
 * @param isRefreshing 当前是否正在下拉刷新
 * @param buffer 提前倒数第 N 项自动触发加载（默认 3 项）
 * @param onLoadMore 触发加载下一页的回调
 */
@Composable
fun LazyListState.OnBottomReached(
    loadMoreState: LoadMoreState = LoadMoreState.Idle,
    isRefreshing: Boolean = false,
    buffer: Int = 3,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val currentLoadMoreState by rememberUpdatedState(loadMoreState)
    val currentIsRefreshing by rememberUpdatedState(isRefreshing)

    LaunchedEffect(this) {
        snapshotFlow {
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val isNearBottom = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer
            isNearBottom && currentLoadMoreState == LoadMoreState.Idle && !currentIsRefreshing
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                currentOnLoadMore()
            }
    }
}

/**
 * 瀑布流 LazyVerticalStaggeredGrid 触底预加载监听扩展
 *
 * @param loadMoreState 当前加载更多状态
 * @param isRefreshing 当前是否正在下拉刷新
 * @param buffer 双列瀑布流建议设为 4（相当于提前约 2 行）
 * @param onLoadMore 触发加载下一页的回调
 */
@Composable
fun LazyStaggeredGridState.OnBottomReached(
    loadMoreState: LoadMoreState = LoadMoreState.Idle,
    isRefreshing: Boolean = false,
    buffer: Int = 4,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val currentLoadMoreState by rememberUpdatedState(loadMoreState)
    val currentIsRefreshing by rememberUpdatedState(isRefreshing)

    LaunchedEffect(this) {
        snapshotFlow {
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: -1
            val isNearBottom = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer
            isNearBottom && currentLoadMoreState == LoadMoreState.Idle && !currentIsRefreshing
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                currentOnLoadMore()
            }
    }
}

/**
 * 通用商业级底部加载状态卡片 (Footer)
 */
@Composable
fun LoadMoreFooter(
    state: LoadMoreState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            LoadMoreState.Idle -> Unit
            LoadMoreState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在加载更多...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LoadMoreState.NoMore -> {
                Text(
                    text = "— 已经到底啦 —",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            is LoadMoreState.Error -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onRetry)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${state.message}，点击重试 🔄",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 官方 Material 3 下拉刷新 + 无感加载更多通用单列列表容器 (Slot API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPagingLazyColumn(
    isRefreshing: Boolean,
    loadMoreState: LoadMoreState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isPullToRefreshEnabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    buffer: Int = 3,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: LazyListScope.() -> Unit
) {
    if (isPullToRefreshEnabled) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier
        ) {
            LazyColumn(
                state = state,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                modifier = Modifier.fillMaxSize()
            ) {
                content()

                if (loadMoreState != LoadMoreState.Idle) {
                    item(
                        key = "paging_load_more_footer",
                        contentType = "paging_load_more_footer"
                    ) {
                        LoadMoreFooter(
                            state = loadMoreState,
                            onRetry = onRetryLoadMore
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            modifier = modifier.fillMaxSize()
        ) {
            content()

            if (loadMoreState != LoadMoreState.Idle) {
                item(
                    key = "paging_load_more_footer",
                    contentType = "paging_load_more_footer"
                ) {
                    LoadMoreFooter(
                        state = loadMoreState,
                        onRetry = onRetryLoadMore
                    )
                }
            }
        }
    }

    state.OnBottomReached(
        loadMoreState = loadMoreState,
        isRefreshing = isRefreshing,
        buffer = buffer,
        onLoadMore = onLoadMore
    )
}

/**
 * 官方 Material 3 下拉刷新 + 无感加载更多小红书双列瀑布流容器 (Slot API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPagingStaggeredGrid(
    isRefreshing: Boolean,
    loadMoreState: LoadMoreState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isPullToRefreshEnabled: Boolean = true,
    columns: StaggeredGridCells = StaggeredGridCells.Fixed(2),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    buffer: Int = 4,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalItemSpacing: Dp = 8.dp,
    content: LazyStaggeredGridScope.() -> Unit
) {
    if (isPullToRefreshEnabled) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier
        ) {
            LazyVerticalStaggeredGrid(
                columns = columns,
                state = state,
                contentPadding = contentPadding,
                horizontalArrangement = horizontalArrangement,
                verticalItemSpacing = verticalItemSpacing,
                modifier = Modifier.fillMaxSize()
            ) {
                content()

                if (loadMoreState != LoadMoreState.Idle) {
                    item(
                        key = "paging_staggered_grid_footer",
                        contentType = "paging_staggered_grid_footer",
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        LoadMoreFooter(
                            state = loadMoreState,
                            onRetry = onRetryLoadMore
                        )
                    }
                }
            }
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = columns,
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalItemSpacing = verticalItemSpacing,
            modifier = modifier.fillMaxSize()
        ) {
            content()

            if (loadMoreState != LoadMoreState.Idle) {
                item(
                    key = "paging_staggered_grid_footer",
                    contentType = "paging_staggered_grid_footer",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    LoadMoreFooter(
                        state = loadMoreState,
                        onRetry = onRetryLoadMore
                    )
                }
            }
        }
    }

    state.OnBottomReached(
        loadMoreState = loadMoreState,
        isRefreshing = isRefreshing,
        buffer = buffer,
        onLoadMore = onLoadMore
    )
}
