package com.lyf.androidmvp.feature.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lyf.androidmvp.core.designsystem.component.AppEmptyView
import com.lyf.androidmvp.core.designsystem.component.AppLoadingView
import com.lyf.androidmvp.core.designsystem.component.AppTopBar
import com.lyf.androidmvp.domain.model.Repo

/**
 * 详情页顶部栏组件
 */
@Composable
fun DetailTopBar(
    title: String,
    isFavorite: Boolean,
    showFavoriteAction: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppTopBar(
        title = title,
        canNavigateBack = true,
        onNavigateBack = onBackClick,
        actions = {
            if (showFavoriteAction) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * 详情页主体状态路由分发区
 */
@Composable
fun DetailBodyContent(
    state: DetailUiState,
    fromSource: String,
    onNavigateBack: () -> Unit,
    onUpdateRemark: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onReturnResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                AppLoadingView(message = "正在加载详情...")
            }
            state.repo == null -> {
                AppEmptyView(
                    message = state.errorMessage ?: "未找到项目详情",
                    onAction = onNavigateBack,
                    actionLabel = "返回上一页"
                )
            }
            else -> {
                val repo = state.repo
                DetailLoadedContent(
                    repo = repo,
                    fromSource = fromSource,
                    userRemark = state.userRemark,
                    isSaving = state.isSaving,
                    onUpdateRemark = onUpdateRemark,
                    onToggleFavorite = onToggleFavorite,
                    onReturnResult = onReturnResult
                )
            }
        }
    }
}

/**
 * 详情内容区（成功态，平铺卡片架构）
 */
@Composable
private fun DetailLoadedContent(
    repo: Repo,
    fromSource: String,
    userRemark: String,
    isSaving: Boolean,
    onUpdateRemark: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onReturnResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailSourceChip(fromSource = fromSource)
        DetailHeaderCard(repo = repo)
        DetailDescriptionCard(description = repo.description)
        DetailResultActionCard(
            isFavorite = repo.isFavorite,
            userRemark = userRemark,
            isSaving = isSaving,
            onUpdateRemark = onUpdateRemark,
            onToggleFavorite = onToggleFavorite,
            onReturnResult = onReturnResult
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 来源入参提示 Chip
 */
@Composable
fun DetailSourceChip(fromSource: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Nav3 强类型传参来源: $fromSource",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 项目作者与核心指标卡片
 */
@Composable
fun DetailHeaderCard(repo: Repo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = repo.ownerAvatarUrl,
                contentDescription = repo.ownerName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = repo.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Author: ${repo.ownerName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Star,
                    value = "${repo.stars}",
                    label = "Stars"
                )
                StatItem(
                    icon = Icons.AutoMirrored.Filled.CallSplit,
                    value = "${repo.forks}",
                    label = "Forks"
                )
                StatItem(
                    icon = Icons.Default.Language,
                    value = repo.language ?: "N/A",
                    label = "Language"
                )
            }
        }
    }
}

/**
 * 项目描述卡片
 */
@Composable
fun DetailDescriptionCard(description: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "项目描述",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description ?: "该仓库暂无描述信息",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 收藏与备注编辑卡片
 */
@Composable
fun DetailResultActionCard(
    isFavorite: Boolean,
    userRemark: String,
    isSaving: Boolean,
    onUpdateRemark: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onReturnResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "收藏与备注",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "收藏状态会自动同步到首页。填写备注后，点击保存或返回即可保存，清理缓存会保留收藏和备注。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userRemark,
                enabled = !isSaving,
                onValueChange = onUpdateRemark,
                label = { Text("项目备注（可选）") },
                placeholder = { Text("例如：已完成商用代码评估，确认集成") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onToggleFavorite,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isFavorite) "取消收藏当前项目" else "加入收藏当前项目")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onReturnResult,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isSaving) "正在保存…" else "保存并返回首页")
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
