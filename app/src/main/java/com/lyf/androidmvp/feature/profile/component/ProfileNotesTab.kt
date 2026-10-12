package com.lyf.androidmvp.feature.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.feature.main.RedBrandColor

/**
 * Tab 0: 笔记列表内容
 */
@Composable
fun NotesTabContent(
    modifier: Modifier = Modifier
) {
    val noteItems = listOf(
        Triple(
            "如何保留 Compose 底部 Tab 的页面状态？",
            "通过 HorizontalPager(beyondViewportPageCount = 3) 配合 Nav3 强类型路由，既保证了全屏子页面的纯正导航回退，又消除了 Tab 切换闪烁！",
            "1.2万赞"
        ),
        Triple(
            "基于 Miuix 与 Haze 打造沉浸式 Android 磨砂毛玻璃",
            "利用 dev.chrisbanes.haze 库实现 60fps 实时高斯模糊，在滑动时顶栏呈现通透磨砂玻璃质感，视觉高级感直接拉满。",
            "8.6千赞"
        ),
        Triple(
            "XXPermissions 在 Compose 中的极简实践",
            "配合 Kotlin 协程与 ActivityResultLauncher，消除原生 onRequestPermissionsResult 样板代码，实现优雅授权。",
            "6.4千赞"
        ),
        Triple(
            "WanAndroid + Clean Architecture 商业级实战",
            "严格遵守 单一数据源 (SSOT) 规范，结合 MVI 架构与冷流响应式状态驱动，打造坚如磐石的商业级移动端架构。",
            "9.9千赞"
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(noteItems) { (title, desc, likes) ->
            ProfileNoteCard(title = title, desc = desc, likes = likes)
        }
    }
}

/**
 * 扁平化笔记卡片
 */
@Composable
private fun ProfileNoteCard(
    title: String,
    desc: String,
    likes: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RedBrandColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "# 精选笔记",
                        style = MaterialTheme.typography.labelSmall,
                        color = RedBrandColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = RedBrandColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = likes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
