package com.lyf.androidmvp.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.feature.home.HomeScreen
import com.lyf.androidmvp.feature.detail.DetailResult
import com.lyf.androidmvp.feature.message.MessageScreen
import com.lyf.androidmvp.feature.profile.ProfileScreen
import com.lyf.androidmvp.feature.video.VideoScreen
import kotlinx.coroutines.launch

/**
 * 主容器多 Tab 懒加载与持久保活横向滚动视图
 */
@Composable
fun MainTabsPager(
    pagerState: PagerState,
    visitedTabs: Set<Int>,
    onNavigateToDetail: (Long, String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    detailResult: DetailResult? = null,
    onDetailResultConsumed: (DetailResult) -> Unit = {}
) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        beyondViewportPageCount = 3,
        modifier = modifier
    ) { page ->
        if (page in visitedTabs) {
            when (page) {
                0 -> HomeScreen(
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToSettings = onNavigateToSettings,
                    detailResult = detailResult,
                    onDetailResultConsumed = onDetailResultConsumed,
                    isActive = pagerState.currentPage == page
                )
                1 -> VideoScreen()
                2 -> MessageScreen()
                3 -> ProfileScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * 小红书 "+" 发布操作弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        PublishSheetContent(
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            }
        )
    }
}

/**
 * 发布抽屉内部选项列表
 */
@Composable
fun PublishSheetContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "发布笔记",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PublishItem(icon = Icons.Default.Image, title = "发图文", color = Color(0xFF26A69A), onClick = onDismiss)
            PublishItem(icon = Icons.Default.Videocam, title = "拍视频", color = RedBrandColor, onClick = onDismiss)
            PublishItem(icon = Icons.Default.CameraAlt, title = "开直播", color = Color(0xFFFFA000), onClick = onDismiss)
            PublishItem(icon = Icons.Default.Drafts, title = "存草稿", color = Color(0xFF7E57C2), onClick = onDismiss)
        }
    }
}

/**
 * 发布入口单个图标按钮
 */
@Composable
fun PublishItem(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
