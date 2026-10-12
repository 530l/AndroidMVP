package com.lyf.androidmvp.feature.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.androidmvp.core.designsystem.component.CollapsibleStickyTabPager
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.feature.profile.component.ProfileHeaderContainer
import com.lyf.androidmvp.feature.profile.component.ProfilePagerSection
import com.lyf.androidmvp.feature.profile.component.ProfileTabLayout

/**
 * 个人中心页面 (Stateful 状态容器)
 * 职责：绑定 ProfileViewModel，获取用户登录信息与真实收藏数据。
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Long, String, String) -> Unit = { _, _, _ -> },
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        state = state,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDetail = onNavigateToDetail,
        onToggleFavorite = { viewModel.onEvent(ProfileUiEvent.ToggleFavorite(it)) },
        modifier = modifier
    )
}

/**
 * 个人中心无状态内容组件 (Stateless)
 * 职责：完全消除金字塔式嵌套，仅以语义化区块平铺组装可折叠头部、吸顶 Tab 与横向分页。
 */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Long, String, String) -> Unit = { _, _, _ -> },
    onToggleFavorite: (Repo) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 3 个子 Tab: 笔记(0)、收藏(1)、赞过(2)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    CollapsibleStickyTabPager(
        modifier = modifier.statusBarsPadding(),
        headerContent = { collapseFraction ->
            ProfileHeaderContainer(
                state = state,
                collapseFraction = collapseFraction,
                onNavigateToSettings = onNavigateToSettings,
                onEditProfile = onEditProfile,
                onShareClick = onShareClick
            )
        },
        stickyTabContent = {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileTabLayout(
                    pagerState = pagerState,
                    notesCount = state.notesCount,
                    favoritesCount = state.favoriteRepos.size,
                    likesCount = state.likesCount
                )
            }
        },
        pagerContent = {
            ProfilePagerSection(
                pagerState = pagerState,
                state = state,
                onNavigateToDetail = onNavigateToDetail,
                onToggleFavorite = onToggleFavorite
            )
        }
    )
}
