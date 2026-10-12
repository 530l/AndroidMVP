package com.lyf.androidmvp.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.domain.repository.RepoRepository
import com.lyf.androidmvp.domain.usecase.SaveRepoRemarkUseCase
import com.lyf.androidmvp.domain.usecase.ToggleFavoriteUseCase
import com.lyf.androidmvp.feature.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val KEY_REPO_ID = "key_repo_id"
private const val KEY_REPO_NAME = "key_repo_name"
private const val KEY_FROM_SOURCE = "key_from_source"
private const val KEY_USER_REMARK = "key_user_remark"
private const val KEY_INITIAL_REMARK = "key_initial_remark"
private const val KEY_INITIAL_FAVORITE = "key_initial_favorite"

/**
 * 详情页 ViewModel：
 *
 * 【架构设计与 UseCase 权衡实践】：
 * 1. 只读数据流直连 Repository（消除形式主义）：
 *    - 详情只读 Flow 观察（[repository.getRepoById]）为纯数据传递，无业务加工，直连 [RepoRepository]，消除单行透传类。
 * 2. 业务状态反转与校验消费 UseCase（充血业务）：
 *    - 切换收藏消费 [ToggleFavoriteUseCase]（存在性校验 + 状态反转 + 持久化，与首页无缝复用）。
 *    - 保存备注消费 [SaveRepoRemarkUseCase]（空白字符裁剪 + 200 字上限校验 + 脏检查防多余 I/O）。
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: RepoRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveRepoRemarkUseCase: SaveRepoRemarkUseCase
) : BaseViewModel<DetailUiState, DetailUiEvent>(
    DetailUiState(
        userRemark = savedStateHandle.get<String>(KEY_USER_REMARK) ?: "",
        fromSource = savedStateHandle.get<String>(KEY_FROM_SOURCE) ?: ""
    ),
    { state, messages -> state.copy(messages = messages) }
) {
    var repoId: Long = savedStateHandle[KEY_REPO_ID] ?: 0L
        private set
    var repoName: String = savedStateHandle[KEY_REPO_NAME] ?: ""
        private set
    var fromSource: String = savedStateHandle[KEY_FROM_SOURCE] ?: ""
        private set

    private var detailJob: Job? = null
    private var favoriteJob: Job? = null
    private val operationMutex = Mutex()

    fun initialize(id: Long, name: String, source: String) {
        if (repoId == id && detailJob?.isActive == true) return
        if (repoId != id) {
            savedStateHandle.remove<String>(KEY_USER_REMARK)
            savedStateHandle.remove<String>(KEY_INITIAL_REMARK)
            savedStateHandle.remove<Boolean>(KEY_INITIAL_FAVORITE)
        }
        savedStateHandle[KEY_REPO_ID] = id
        savedStateHandle[KEY_REPO_NAME] = name
        savedStateHandle[KEY_FROM_SOURCE] = source
        repoId = id
        repoName = name
        fromSource = source
        detailJob?.cancel()
        updateState { it.copy(fromSource = source, isLoading = true) }
        detailJob = viewModelScope.launch {
            repository.getRepoById(id).catch { exception ->
                updateState { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
                showMessage(exception.localizedMessage ?: "读取详情失败")
            }.collect { repo ->
                if (repo != null && !savedStateHandle.contains(KEY_INITIAL_REMARK)) {
                    savedStateHandle[KEY_INITIAL_REMARK] = repo.userRemark
                    savedStateHandle[KEY_INITIAL_FAVORITE] = repo.isFavorite
                }
                if (repo != null && !savedStateHandle.contains(KEY_USER_REMARK)) {
                    savedStateHandle[KEY_USER_REMARK] = repo.userRemark
                }
                updateState {
                    it.copy(
                        isLoading = false,
                        repo = repo,
                        userRemark = savedStateHandle[KEY_USER_REMARK] ?: it.userRemark,
                        errorMessage = if (repo == null) "未找到该项目详情" else null
                    )
                }
            }
        }
    }

    override fun onEvent(event: DetailUiEvent) {
        if (uiState.value.isSaving || uiState.value.isExitReady) return
        when (event) {
            DetailUiEvent.ToggleFavorite -> toggleFavorite()
            is DetailUiEvent.UpdateRemark -> {
                savedStateHandle[KEY_USER_REMARK] = event.remark
                updateState { it.copy(userRemark = event.remark) }
            }
            DetailUiEvent.ReturnResultAndBack -> saveAndFinish(forceResult = true)
            DetailUiEvent.ClickBack -> saveAndFinish(forceResult = false)
        }
    }

    private fun toggleFavorite() {
        if (uiState.value.repo == null || favoriteJob?.isActive == true) return
        favoriteJob = viewModelScope.launch {
            try {
                operationMutex.withLock {
                    when (val result = toggleFavoriteUseCase(repoId)) {
                        is AppResult.Success -> {
                            val isFav = result.data
                            showMessage(if (isFav) "已收藏" else "已取消收藏")
                        }
                        is AppResult.Error -> {
                            showMessage(result.exception.message)
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showMessage(exception.localizedMessage ?: "收藏操作失败，请重试")
            }
        }
    }

    fun saveAndGetResult(forceResult: Boolean = false): DetailResult? {
        val storedRepo = uiState.value.repo
        val remark = savedStateHandle.get<String>(KEY_USER_REMARK)
            ?: storedRepo?.userRemark.orEmpty()
        val initialRemark = savedStateHandle.get<String>(KEY_INITIAL_REMARK)
            ?: storedRepo?.userRemark.orEmpty()
        val initialFavorite = savedStateHandle.get<Boolean>(KEY_INITIAL_FAVORITE)
            ?: storedRepo?.isFavorite ?: false
        val favorite = storedRepo?.isFavorite ?: false
        val changed = remark != initialRemark || favorite != initialFavorite

        if (remark != initialRemark) {
            viewModelScope.launch {
                try {
                    saveRepoRemarkUseCase(repoId, remark)
                } catch (e: Exception) {
                    // 退出时后台异步持久化，不阻断主线程动画
                }
            }
        }
        return if (forceResult || changed) DetailResult(repoId, favorite, remark.ifBlank { null }) else null
    }

    private fun saveAndFinish(forceResult: Boolean) {
        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                // 等待已开始的收藏写入，并在所有返回入口统一持久化备注。
                favoriteJob?.join()
                val result = operationMutex.withLock {
                    val storedRepo = repository.getRepoById(repoId).first()
                    // 首次查询尚未完成时返回，沿用数据库备注，不能用空草稿覆盖已有备注。
                    val remark = savedStateHandle.get<String>(KEY_USER_REMARK)
                        ?: storedRepo?.userRemark.orEmpty()
                    when (val saveResult = saveRepoRemarkUseCase(repoId, remark)) {
                        is AppResult.Error -> showMessage(saveResult.exception.message)
                        is AppResult.Success -> Unit
                    }
                    val currentRepo = repository.getRepoById(repoId).first()
                    val initialRemark = savedStateHandle.get<String>(KEY_INITIAL_REMARK)
                        ?: storedRepo?.userRemark.orEmpty()
                    val initialFavorite = savedStateHandle.get<Boolean>(KEY_INITIAL_FAVORITE)
                        ?: storedRepo?.isFavorite ?: false
                    val favorite = currentRepo?.isFavorite ?: uiState.value.repo?.isFavorite ?: false
                    val changed = remark != initialRemark || favorite != initialFavorite
                    if (forceResult || changed) DetailResult(repoId, favorite, remark.ifBlank { null }) else null
                }
                // 保存成功是业务状态，UI 在前台观察到此状态后负责退栈。
                updateState { it.copy(isExitReady = true, exitResult = result) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showMessage(exception.localizedMessage ?: "保存备注失败，请重试")
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }
}
