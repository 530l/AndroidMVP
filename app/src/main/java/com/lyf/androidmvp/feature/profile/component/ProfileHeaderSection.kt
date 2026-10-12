package com.lyf.androidmvp.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyf.androidmvp.feature.main.RedBrandColor

/**
 * 个人资料头像与简介区域 (扁平化平铺)
 */
@Composable
fun ProfileHeaderSection(
    username: String? = null,
    isLoggedIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(username = username)

            Spacer(modifier = Modifier.width(16.dp))

            ProfileUserInfo(username = username, isLoggedIn = isLoggedIn)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "专注 Android 官方最新规范架构，Nav3 单体极速脚手架，开箱即用 🚀",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 扁平化独立头像组件
 */
@Composable
private fun ProfileAvatar(
    username: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(RedBrandColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username?.take(3)?.uppercase() ?: "RED",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = RedBrandColor
        )
    }
}

/**
 * 扁平化独立用户名及认证标签
 */
@Composable
private fun ProfileUserInfo(
    username: String?,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = username ?: "商业级移动架构师",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isLoggedIn) "已认证开发者 · 小红书号：888666999" else "小红书号：888666999",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
