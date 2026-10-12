package com.lyf.androidmvp.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyf.androidmvp.core.common.permission.AppPermissionResult
import com.lyf.androidmvp.core.common.permission.AppPermissions
import com.lyf.androidmvp.core.common.permission.isPermissionGranted
import com.lyf.androidmvp.core.common.permission.openPermissionSettings
import com.lyf.androidmvp.core.common.permission.requestAppPermissions
import com.lyf.androidmvp.core.datastore.AppThemeConfig
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.RadioButton as MiuixRadioButton

/**
 * 设置项图标容器组件 (Squircle 柔色圆角徽标)
 */
@Composable
fun SettingIconBadge(
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = iconColor.copy(alpha = 0.12f)
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )
    }
}

/**
 * 分组小标题
 */
@Composable
fun SettingSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
        modifier = modifier.padding(start = 12.dp, top = 8.dp, bottom = 6.dp)
    )
}

/**
 * 分组卡片容器 (基于 Miuix Squircle 卡片)
 */
@Composable
fun SettingGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * 分组内部缩进分割线 (避开左侧图标区域)
 */
@Composable
fun SettingInsetDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = 62.dp
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startIndent),
        thickness = 0.6.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

/**
 * 通用设置项单行组件 (支持左侧徽标、标题、副标题、右侧自定义动作)
 */
@Composable
fun SettingItemRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    endContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            SettingIconBadge(
                icon = icon,
                iconColor = iconColor
            )
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        if (endContent != null) {
            Spacer(modifier = Modifier.width(12.dp))
            endContent()
        }
    }
}

/**
 * 个人账户卡片 (横幅风格，增强视觉品质)
 */
@Composable
fun AccountSettingsCard(
    username: String?,
    token: String?,
    onSaveToken: (String, String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoggedIn = !token.isNullOrBlank()

    SettingGroupCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像徽标
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isLoggedIn) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = if (isLoggedIn) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = username ?: "未登录访客",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isLoggedIn) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "已连接",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isLoggedIn) {
                            "Token: ${token?.take(12)}... (DataStore)"
                        } else {
                            "登录后可体验收藏同步与个性化配置"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 底部操作按钮栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoggedIn) {
                    MiuixButton(
                        onClick = {
                            onSaveToken("ghp_commercial_mock_token_123456", "EnterpriseDeveloper")
                        },
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp,
                        minHeight = 36.dp,
                        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "更新模拟凭据",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    MiuixButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp,
                        minHeight = 36.dp,
                        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "退出登录",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    MiuixButton(
                        onClick = {
                            onSaveToken("ghp_commercial_mock_token_123456", "EnterpriseDeveloper")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp,
                        minHeight = 40.dp,
                        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "一键模拟登录 (EnterpriseDeveloper)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 主题设置卡片 (外观与显示)
 */
@Composable
fun ThemeSettingsCard(
    themeConfig: AppThemeConfig,
    onThemeChange: (AppThemeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingSectionTitle(text = "外观与显示")
        SettingGroupCard {
            SettingItemRow(
                title = "跟随系统",
                subtitle = "根据系统设置自动切换深浅色模式",
                icon = Icons.Default.BrightnessAuto,
                iconColor = Color(0xFF6366F1),
                onClick = { onThemeChange(AppThemeConfig.FOLLOW_SYSTEM) },
                endContent = {
                    MiuixRadioButton(
                        selected = themeConfig == AppThemeConfig.FOLLOW_SYSTEM,
                        onClick = { onThemeChange(AppThemeConfig.FOLLOW_SYSTEM) }
                    )
                }
            )

            SettingInsetDivider()

            SettingItemRow(
                title = "浅色模式",
                subtitle = "明亮清新的应用界面风格",
                icon = Icons.Default.LightMode,
                iconColor = Color(0xFFF59E0B),
                onClick = { onThemeChange(AppThemeConfig.LIGHT) },
                endContent = {
                    MiuixRadioButton(
                        selected = themeConfig == AppThemeConfig.LIGHT,
                        onClick = { onThemeChange(AppThemeConfig.LIGHT) }
                    )
                }
            )

            SettingInsetDivider()

            SettingItemRow(
                title = "深色模式",
                subtitle = "暗黑护眼的主题界面风格",
                icon = Icons.Default.DarkMode,
                iconColor = Color(0xFF8B5CF6),
                onClick = { onThemeChange(AppThemeConfig.DARK) },
                endContent = {
                    MiuixRadioButton(
                        selected = themeConfig == AppThemeConfig.DARK,
                        onClick = { onThemeChange(AppThemeConfig.DARK) }
                    )
                }
            )
        }
    }
}

/**
 * 系统权限管理卡片 (XXPermissions 驱动)
 */
@Composable
fun PermissionSettingsCard(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isNotificationGranted by remember {
        mutableStateOf(context.isPermissionGranted(AppPermissions.Notification))
    }
    var isCameraGranted by remember {
        mutableStateOf(context.isPermissionGranted(AppPermissions.Camera))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingSectionTitle(text = "系统权限与隐私 (XXPermissions)")
        SettingGroupCard {
            // 通知权限
            SettingItemRow(
                title = "系统通知权限",
                subtitle = "用于接收推送通知、系统消息与后台状态",
                icon = Icons.Default.Notifications,
                iconColor = Color(0xFFF97316),
                endContent = {
                    if (isNotificationGranted) {
                        GrantedStatusBadge()
                    } else {
                        RequestPermissionButton(
                            onClick = {
                                coroutineScope.launch {
                                    when (val result = context.requestAppPermissions(AppPermissions.Notification)) {
                                        is AppPermissionResult.Granted -> {
                                            isNotificationGranted = true
                                            onShowMessage("通知权限已授予")
                                        }
                                        is AppPermissionResult.Denied -> {
                                            isNotificationGranted = false
                                            if (result.doNotAskAgain) {
                                                onShowMessage("通知权限被永久拒绝，前往系统设置开启")
                                                context.openPermissionSettings(AppPermissions.Notification)
                                            } else {
                                                onShowMessage("通知权限已被拒绝")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )

            SettingInsetDivider()

            // 相机权限
            SettingItemRow(
                title = "相机拍摄权限",
                subtitle = "用于拍照取景、扫一扫与多媒体功能",
                icon = Icons.Default.CameraAlt,
                iconColor = Color(0xFF0284C7),
                endContent = {
                    if (isCameraGranted) {
                        GrantedStatusBadge()
                    } else {
                        RequestPermissionButton(
                            onClick = {
                                coroutineScope.launch {
                                    when (val result = context.requestAppPermissions(AppPermissions.Camera)) {
                                        is AppPermissionResult.Granted -> {
                                            isCameraGranted = true
                                            onShowMessage("相机权限已授予")
                                        }
                                        is AppPermissionResult.Denied -> {
                                            isCameraGranted = false
                                            if (result.doNotAskAgain) {
                                                onShowMessage("相机权限被永久拒绝，前往系统设置开启")
                                                context.openPermissionSettings(AppPermissions.Camera)
                                            } else {
                                                onShowMessage("相机权限已被拒绝")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

/**
 * 已授权状态药丸徽标
 */
@Composable
private fun GrantedStatusBadge() {
    Surface(
        color = Color(0xFF10B981).copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF10B981), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "已授权",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF059669)
            )
        }
    }
}

/**
 * 申请授权操作按钮
 */
@Composable
private fun RequestPermissionButton(onClick: () -> Unit) {
    MiuixButton(
        onClick = onClick,
        cornerRadius = 12.dp,
        minHeight = 32.dp,
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "去开启",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 存储与缓存设置卡片 (Room Cache)
 */
@Composable
fun CacheSettingsCard(
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingSectionTitle(text = "存储与数据")
        SettingGroupCard {
            SettingItemRow(
                title = "离线数据缓存",
                subtitle = "Room 本地离线数据库与网络请求响应",
                icon = Icons.Default.Storage,
                iconColor = Color(0xFF8B5CF6),
                endContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "约 2.4 MB",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )

            SettingInsetDivider()

            SettingItemRow(
                title = "清理项目缓存",
                subtitle = "清理离线项目数据，保留收藏与备注标记",
                icon = Icons.Default.DeleteSweep,
                iconColor = Color(0xFFEF4444),
                endContent = {
                    MiuixButton(
                        onClick = onClearCache,
                        cornerRadius = 12.dp,
                        minHeight = 32.dp,
                        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "立即清理",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    }
}

/**
 * 关于与技术架构卡片
 */
@Composable
fun AboutSettingsCard(
    appVersion: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingSectionTitle(text = "关于与技术架构")
        SettingGroupCard {
            SettingItemRow(
                title = "应用版本",
                subtitle = "WanAndroid MVP 商业版发布",
                icon = Icons.Default.Info,
                iconColor = Color(0xFF10B981),
                endContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "v$appVersion",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )

            SettingInsetDivider()

            SettingItemRow(
                title = "核心技术体系",
                subtitle = "Kotlin 2.2 · Miuix · Haze · Room",
                icon = Icons.Default.Widgets,
                iconColor = Color(0xFF0284C7),
                endContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            )
        }
    }
}
