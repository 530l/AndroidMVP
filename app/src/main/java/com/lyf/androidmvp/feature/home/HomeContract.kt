package com.lyf.androidmvp.feature.home

import com.lyf.androidmvp.core.designsystem.component.paging.LoadMoreState
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.core.common.UiMessage
import com.lyf.androidmvp.feature.base.UiEvent
import com.lyf.androidmvp.feature.base.UiState

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadMoreState: LoadMoreState = LoadMoreState.Idle,
    val page: Int = 1,
    val repos: List<Repo> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    override val messages: List<UiMessage> = emptyList()
) : UiState {
    // 性能优化：通过 lazy 缓存过滤结果，避免在 Compose 重组周期中重复执行 filter 遍历
    val filteredRepos: List<Repo> by lazy(LazyThreadSafetyMode.NONE) {
        if (searchQuery.isBlank()) {
            repos
        } else {
            repos.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.description?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }
}

sealed interface HomeUiEvent : UiEvent {
    data object LoadData : HomeUiEvent
    data object Refresh : HomeUiEvent
    data object LoadMore : HomeUiEvent
    data object RetryLoadMore : HomeUiEvent
    data class SearchQueryChanged(val query: String) : HomeUiEvent
    data class ToggleFavorite(val repo: Repo) : HomeUiEvent
}
