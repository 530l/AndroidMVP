package com.lyf.androidmvp.feature.home

import androidx.lifecycle.viewModelScope
import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.core.common.NetworkMonitor
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.domain.repository.RepoRepository
import com.lyf.androidmvp.domain.usecase.ToggleFavoriteUseCase
import com.lyf.androidmvp.feature.base.BaseViewModel
import com.lyf.androidmvp.feature.detail.DetailResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.lyf.androidmvp.core.designsystem.component.paging.LoadMoreState
import kotlinx.coroutines.delay

/**
 * 首页 ViewModel：
 *
 * 【架构设计与 UseCase 权衡实践】：
 * 1. 纯数据流直连 Repository（消除形式主义）：
 *    - 列表本地 Flow 观察（[repository.getRepos]）、网络刷新（[repository.refreshRepos]）与分页（[repository.loadMoreRepos]）
 *      不包含业务校验或复杂状态转换，属于纯数据流，直接面向 [RepoRepository]，杜绝空壳透传用例。
 * 2. 核心业务操作消费 UseCase（充血业务与跨 VM 复用）：
 *    - 切换收藏操作消费 [ToggleFavoriteUseCase]，业务逻辑（查库 -> 状态反转 -> 持久化）完全由 Domain 闭环，
 *      消灭 ViewModel 中的业务泄露与重复代码。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepoRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val networkMonitor: NetworkMonitor
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState(), { state, messages ->
    state.copy(messages = messages)
}) {
    private var refreshJob: Job? = null
    private var loadMoreJob: Job? = null
    private var lastResultId: String? = null
    private val favoriteJobs = mutableMapOf<Long, Job>()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                updateState { it.copy(isOnline = isOnline) }
            }
        }
        // 初始化时只建立一个数据库订阅，刷新不会重新订阅。
        viewModelScope.launch {
            repository.getRepos().catch { exception ->
                updateState { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
                showMessage(exception.localizedMessage ?: "读取本地数据失败")
            }.collect { repos ->
                updateState { it.copy(repos = repos, isLoading = it.isLoading && repos.isEmpty()) }
            }
        }
        loadRepos(forceRefresh = false)
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.LoadData -> loadRepos(forceRefresh = false)
            HomeUiEvent.Refresh -> loadRepos(forceRefresh = true)
            HomeUiEvent.LoadMore -> loadMoreRepos()
            HomeUiEvent.RetryLoadMore -> loadMoreRepos()
            is HomeUiEvent.SearchQueryChanged -> updateState { it.copy(searchQuery = event.query) }
            is HomeUiEvent.ToggleFavorite -> toggleFavorite(event.repo)
        }
    }

    fun acceptDetailResult(result: DetailResult) {
        if (lastResultId == result.resultId) return
        lastResultId = result.resultId
        val status = if (result.isFavorite) "已收藏" else "未收藏"
        val remark = result.userRemark?.let { "，备注：$it" }.orEmpty()
        // 数据通过 Room 自动同步，回参只生成提示，不再次修改项目状态。
        showMessage("项目 ${result.repoId} 已保存：$status$remark")
    }

    private fun loadRepos(forceRefresh: Boolean) {
        if (refreshJob?.isActive == true) return
        loadMoreJob?.cancel()
        updateState {
            it.copy(isLoading = it.repos.isEmpty(), isRefreshing = forceRefresh, errorMessage = null)
        }
        refreshJob = viewModelScope.launch {
            try {
                when (val result = repository.refreshRepos(forceRefresh)) {
                    is AppResult.Success -> {
                        updateState { it.copy(page = 1, loadMoreState = LoadMoreState.Idle) }
                    }
                    is AppResult.Error -> {
                        updateState { it.copy(errorMessage = result.exception.message) }
                        showMessage(result.exception.message)
                    }
                }
            } finally {
                // 刷新状态只由网络操作完成与否决定，不受缓存发射影响。
                updateState { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private fun toggleFavorite(repo: Repo) {
        if (favoriteJobs[repo.id]?.isActive == true) return
        favoriteJobs[repo.id] = viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(repo.id)) {
                    is AppResult.Success -> {
                        val isFav = result.data
                        showMessage(if (isFav) "已添加收藏" else "已取消收藏")
                    }
                    is AppResult.Error -> {
                        showMessage(result.exception.message)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showMessage(exception.localizedMessage ?: "收藏操作失败，请重试")
            }
        }
    }

    private fun loadMoreRepos() {
        val currentState = uiState.value
        if (loadMoreJob?.isActive == true ||
            currentState.loadMoreState is LoadMoreState.Loading ||
            currentState.loadMoreState is LoadMoreState.NoMore ||
            currentState.isRefreshing ||
            currentState.repos.isEmpty()
        ) {
            return
        }

        updateState { it.copy(loadMoreState = LoadMoreState.Loading) }
        loadMoreJob = viewModelScope.launch {
            try {
                val pageToFetch = currentState.page
                // 保证至少有微小过渡（350ms），确保用户滑动触底时能清晰看到转圈动画，提升可感知反馈
                delay(350)
                when (val result = repository.loadMoreRepos(pageToFetch)) {
                    is AppResult.Success -> {
                        val hasMore = result.data
                        updateState {
                            it.copy(
                                loadMoreState = if (hasMore) LoadMoreState.Idle else LoadMoreState.NoMore,
                                page = pageToFetch + 1
                            )
                        }
                    }
                    is AppResult.Error -> {
                        updateState {
                            it.copy(loadMoreState = LoadMoreState.Error(result.exception.message))
                        }
                        showMessage(result.exception.message)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateState {
                    it.copy(loadMoreState = LoadMoreState.Error(exception.localizedMessage ?: "加载下一页失败"))
                }
            }
        }
    }
}
