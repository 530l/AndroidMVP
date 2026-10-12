package com.lyf.androidmvp.core.designsystem.component.paging

import com.lyf.androidmvp.feature.home.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadMoreStateTest {

    @Test
    fun defaultLoadMoreState_isIdle() {
        val state = HomeUiState()
        assertEquals(LoadMoreState.Idle, state.loadMoreState)
        assertEquals(1, state.page)
    }

    @Test
    fun loadMoreState_transitionsCorrectly() {
        val loadingState: LoadMoreState = LoadMoreState.Loading
        assertTrue(loadingState is LoadMoreState.Loading)

        val noMoreState: LoadMoreState = LoadMoreState.NoMore
        assertTrue(noMoreState is LoadMoreState.NoMore)

        val errorState: LoadMoreState = LoadMoreState.Error("网络超时")
        assertTrue(errorState is LoadMoreState.Error)
        assertEquals("网络超时", (errorState as LoadMoreState.Error).message)
    }

    @Test
    fun prefetchCondition_triggersOnlyWhenIdleAndNotRefreshing() {
        fun canTriggerLoadMore(
            totalItems: Int,
            lastVisibleIndex: Int,
            buffer: Int,
            loadMoreState: LoadMoreState,
            isRefreshing: Boolean
        ): Boolean {
            val isNearBottom = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer
            return isNearBottom && loadMoreState == LoadMoreState.Idle && !isRefreshing
        }

        val total = 20
        val buffer = 6
        val triggerThreshold = total - 1 - buffer // 13

        // 1. 未进入预加载阈值 (例如 index 12 < 13)
        assertFalse(canTriggerLoadMore(total, 12, buffer, LoadMoreState.Idle, isRefreshing = false))

        // 2. 进入预加载阈值 (index 13 >= 13)，Idle 且未在刷新 -> 立即触发
        assertTrue(canTriggerLoadMore(total, 13, buffer, LoadMoreState.Idle, isRefreshing = false))

        // 3. 进入预加载阈值，但正在加载中 (Loading) -> 绝不重复触发
        assertFalse(canTriggerLoadMore(total, 15, buffer, LoadMoreState.Loading, isRefreshing = false))

        // 4. 进入预加载阈值，但正在下拉刷新 (Refreshing) -> 不触发
        assertFalse(canTriggerLoadMore(total, 15, buffer, LoadMoreState.Idle, isRefreshing = true))

        // 5. 数据全部加载完毕 (NoMore) -> 不触发
        assertFalse(canTriggerLoadMore(total, 19, buffer, LoadMoreState.NoMore, isRefreshing = false))

        // 6. 加载出错 (Error) -> 不自动触发，等待用户点击重试
        assertFalse(canTriggerLoadMore(total, 19, buffer, LoadMoreState.Error("500"), isRefreshing = false))

        // 7. 新数据返回后 (总数变为 40)，若用户当前在 19，19 < 40 - 1 - 6 (33) -> 自动变为 false，等待用户再次下滑至 33 时触发下一页
        assertFalse(canTriggerLoadMore(40, 19, buffer, LoadMoreState.Idle, isRefreshing = false))
    }

    @Test
    fun prefetchCondition_pageSize10_buffer3_worksSmoothly() {
        fun canTriggerLoadMore(
            totalItems: Int,
            lastVisibleIndex: Int,
            buffer: Int = 3,
            loadMoreState: LoadMoreState = LoadMoreState.Idle,
            isRefreshing: Boolean = false
        ): Boolean {
            val isNearBottom = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer
            return isNearBottom && loadMoreState == LoadMoreState.Idle && !isRefreshing
        }

        val total = 10 // 一页 10 条
        val buffer = 3
        // 临界触发索引：10 - 1 - 3 = 6

        // 首屏展示 0..4 项时，index 4 不触发
        assertFalse(canTriggerLoadMore(total, 4, buffer))

        // 用户轻微滑动至第 6 项时，立即后台触发
        assertTrue(canTriggerLoadMore(total, 6, buffer))

        // 正在加载中不触发
        assertFalse(canTriggerLoadMore(total, 6, buffer, loadMoreState = LoadMoreState.Loading))

        // 下一页 10 条返回后，总数变为 20，当前还在第 6 项，不会立即连环触发下一页
        assertFalse(canTriggerLoadMore(20, 6, buffer))

        // 滑动至第 16 项（20 - 1 - 3）时，触发第 3 页
        assertTrue(canTriggerLoadMore(20, 16, buffer))
    }
}
