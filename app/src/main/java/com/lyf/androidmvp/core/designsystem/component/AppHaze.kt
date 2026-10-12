package com.lyf.androidmvp.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/**
 * 记住一个 Haze 毛玻璃状态控制器
 */
@Composable
fun rememberAppHazeState(): HazeState = remember { HazeState() }

/**
 * 标记为毛玻璃采样源 (通常挂载在可滚动的 LazyColumn、LazyVerticalGrid 或整个页面背景上)
 */
fun Modifier.appHazeSource(state: HazeState): Modifier = this.hazeSource(state)

/**
 * 标记为磨砂玻璃 / 毛玻璃层 (通常挂载在 TopAppBar、BottomBar 或半透悬浮 Card 上)
 *
 * @param state 关联的 HazeState 采样源
 * @param tint 蒙层主色调（通常为半透明背景色，如 MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)）
 * @param blurRadius 模糊半径，数值越大磨砂感越强，默认 20.dp
 * @param noiseFactor 噪点因子（模拟物理毛玻璃的细腻颗粒质感），默认 0.10f
 */
fun Modifier.appFrostedGlass(
    state: HazeState,
    tint: Color = Color.Unspecified,
    blurRadius: Dp = 20.dp,
    noiseFactor: Float = 0.10f
): Modifier = this.hazeEffect(
    state = state,
    style = HazeStyle(
        backgroundColor = Color.Transparent,
        tints = if (tint != Color.Unspecified) listOf(HazeDefaults.tint(tint)) else emptyList(),
        blurRadius = blurRadius,
        noiseFactor = noiseFactor
    )
)
