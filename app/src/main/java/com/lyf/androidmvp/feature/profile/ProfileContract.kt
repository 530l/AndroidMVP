package com.lyf.androidmvp.feature.profile

import com.lyf.androidmvp.core.common.UiMessage
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.base.UiEvent
import com.lyf.androidmvp.feature.base.UiState

data class ProfileUiState(
    val username: String? = null,
    val token: String? = null,
    val isLoggedIn: Boolean = false,
    val favoriteRepos: List<Repo> = emptyList(),
    val notesCount: Int = 4,
    val likesCount: Int = 18,
    override val messages: List<UiMessage> = emptyList()
) : UiState

sealed interface ProfileUiEvent : UiEvent {
    data class ToggleFavorite(val repo: Repo) : ProfileUiEvent
}
