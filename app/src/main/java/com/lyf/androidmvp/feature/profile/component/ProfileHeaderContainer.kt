package com.lyf.androidmvp.feature.profile.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.feature.profile.ProfileUiState

/**
 * 个人中心可折叠头部容器 (组装各横向平铺区块，消灭外层金字塔嵌套)
 */
@Composable
fun ProfileHeaderContainer(
    state: ProfileUiState,
    collapseFraction: Float,
    onNavigateToSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                // 向上滑动折叠时，平滑减弱透明度
                alpha = (1f - collapseFraction * 1.15f).coerceIn(0f, 1f)
            }
            .padding(horizontal = 16.dp)
    ) {
        ProfileTopBar(
            onNavigateToSettings = onNavigateToSettings,
            onShareClick = onShareClick
        )

        ProfileHeaderSection(
            username = state.username,
            isLoggedIn = state.isLoggedIn
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileStatsRow()

        Spacer(modifier = Modifier.height(12.dp))

        MineFunctionCenterGrid()

        Spacer(modifier = Modifier.height(12.dp))

        ProfileActionsRow(
            onNavigateToSettings = onNavigateToSettings,
            onEditProfile = onEditProfile
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
