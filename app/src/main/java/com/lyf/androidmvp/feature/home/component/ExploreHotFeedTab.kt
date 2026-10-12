package com.lyf.androidmvp.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.domain.model.Repo

/**
 * Tab 1: 热门爆款内容页 (探索模式)
 */
@Composable
fun ExploreHotFeedTab(
    onRepoClick: (Repo) -> Unit,
    modifier: Modifier = Modifier
) {
    val hotItems = listOf(
        Triple("Jetpack Compose 1.8 编译全景与性能极致剖析", "Google 官方团队深入讲解 Compose 编译器重组优化与原生绘制管线。", "4.2万人在看"),
        Triple("Kotlin 2.2.10 全新 K2 编译器迁移踩坑全纪录", "从 Gradle 构建缓存到 KSP 代码生成全链路加速经验总结。", "3.8万人在看"),
        Triple("Android 16 边缘全面沉浸与系统预测性手势实战", "带你全面吃透最新 Android 规范要求与回退栈手势系统。", "2.9万人在看")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(hotItems) { (title, desc, views) ->
            ExploreHotFeedCard(title = title, desc = desc, views = views)
        }
    }
}

/**
 * 扁平化热门卡片，消除金字塔
 */
@Composable
private fun ExploreHotFeedCard(
    title: String,
    desc: String,
    views: String,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFF5722).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "🔥 热门推荐",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF5722),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = views,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
