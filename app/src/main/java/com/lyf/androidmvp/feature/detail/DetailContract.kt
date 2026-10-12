package com.lyf.androidmvp.feature.detail

import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.base.UiEvent
import com.lyf.androidmvp.core.common.UiMessage
import com.lyf.androidmvp.feature.base.UiState

data class DetailUiState(
    val isLoading: Boolean = true,
    val repo: Repo? = null,
    val errorMessage: String? = null,
    val fromSource: String = "",
    val userRemark: String = "",
    val isSaving: Boolean = false,
    val isExitReady: Boolean = false,
    val exitResult: DetailResult? = null,
    override val messages: List<UiMessage> = emptyList()
) : UiState

sealed interface DetailUiEvent : UiEvent {
    data object ToggleFavorite : DetailUiEvent
    data class UpdateRemark(val remark: String) : DetailUiEvent
    data object ReturnResultAndBack : DetailUiEvent
    data object ClickBack : DetailUiEvent
}
