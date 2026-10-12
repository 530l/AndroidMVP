package com.lyf.androidmvp.feature.profile

import androidx.lifecycle.viewModelScope
import com.lyf.androidmvp.data.repository.SettingsRepository
import com.lyf.androidmvp.domain.repository.RepoRepository
import com.lyf.androidmvp.feature.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val repoRepository: RepoRepository
) : BaseViewModel<ProfileUiState, ProfileUiEvent>(
    ProfileUiState(),
    { state, _ -> state }
) {
    init {
        viewModelScope.launch {
            combine(
                settingsRepository.username,
                settingsRepository.authToken,
                repoRepository.getFavoriteRepos()
            ) { username, token, favorites ->
                Triple(username, token, favorites)
            }.collect { (username, token, favorites) ->
                updateState {
                    it.copy(
                        username = username,
                        token = token,
                        isLoggedIn = !token.isNullOrBlank(),
                        favoriteRepos = favorites
                    )
                }
            }
        }
    }

    override fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.ToggleFavorite -> launchAction {
                repoRepository.toggleFavorite(event.repo.id, !event.repo.isFavorite)
            }
        }
    }
}
