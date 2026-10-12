package com.lyf.androidmvp.feature.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 视频流主页面 (Stateful 状态容器)
 * 页面内状态：测试 HorizontalPager 切换 Tab 时是否保持（不被重组重置）
 */
@Composable
fun VideoScreen(modifier: Modifier = Modifier) {
    var likeCount by rememberSaveable { mutableIntStateOf(1024) }
    var isLiked by rememberSaveable { mutableStateOf(false) }

    VideoContent(
        likeCount = likeCount,
        isLiked = isLiked,
        onLikeToggle = {
            isLiked = !isLiked
            likeCount += if (isLiked) 1 else -1
        },
        onShareClick = { /* 分享业务逻辑扩展点 */ },
        modifier = modifier
    )
}

/**
 * 纯纯无状态内容组件 (Stateless)
 */
@Composable
fun VideoContent(
    likeCount: Int,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        VideoTopBar()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VideoFeaturedCard(
                    likeCount = likeCount,
                    isLiked = isLiked,
                    onLikeToggle = onLikeToggle,
                    onShareClick = onShareClick
                )
            }

            items(3) { index ->
                VideoPlaceholderCard(index = index)
            }
        }
    }
}
