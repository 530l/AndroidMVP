package com.lyf.androidmvp.feature.profile.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.profile.ProfileUiState

/**
 * 个人中心横向分页内容区 (解耦 HorizontalPager 内部嵌套)
 */
@Composable
fun ProfilePagerSection(
    pagerState: PagerState,
    state: ProfileUiState,
    onNavigateToDetail: (Long, String, String) -> Unit,
    onToggleFavorite: (Repo) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> NotesTabContent(modifier = Modifier.fillMaxSize())
            1 -> FavoritesTabContent(
                favoriteRepos = state.favoriteRepos,
                onRepoClick = onNavigateToDetail,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.fillMaxSize()
            )
            2 -> LikedTabContent(modifier = Modifier.fillMaxSize())
        }
    }
}
