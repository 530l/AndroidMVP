package com.lyf.androidmvp.core.designsystem.component.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.lyf.androidmvp.feature.main.RedBrandColor
import kotlin.math.abs
import kotlin.math.min

/**
 * 轮播图指示器样式策略契约
 *
 * 吸收并升华 tommybuonomo/dotsindicator 的流体物理动画架构：
 * 实时精准同步手势滑动位移，彻底消除传统指示器离散顿挫。
 */
sealed interface BannerIndicatorStyle {
    /**
     * 蠕动伸缩胶囊动画指示器 (灵感源自 tommybuonomo/dotsindicator 的 WormDotsIndicator)
     *
     * 具备真实物理双阶段形变效果：
     * - 前 50% 滑动进度：头部向前延伸，尾部保持锚定，胶囊拉长
     * - 后 50% 滑动进度：头部已触达目标，尾部向前收缩追赶，缩回圆点
     */
    data class Worm(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.45f),
        val dotSize: Dp = 6.dp,
        val spacing: Dp = 6.dp
    ) : BannerIndicatorStyle

    /**
     * 弹性滑动圆点指示器 (灵感源自 tommybuonomo/dotsindicator 的 SpringDotsIndicator)
     *
     * 静态底轨圆点上悬浮平滑滑块，随着手势在圆点之间连续滑动，松手时弹性归位。
     */
    data class Spring(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.45f),
        val dotSize: Dp = 6.dp,
        val selectorSize: Dp = 6.dp,
        val spacing: Dp = 6.dp
    ) : BannerIndicatorStyle

    /**
     * 动态宽度拉伸指示器 (灵感源自 tommybuonomo/dotsindicator 的 ShiftDotsIndicator)
     *
     * 选中项宽度放大为胶囊状，相邻圆点在手势滑动过程中对称平滑过渡。
     */
    data class Shift(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.45f),
        val dotSize: Dp = 6.dp,
        val shiftFactor: Float = 2.8f,
        val spacing: Dp = 6.dp
    ) : BannerIndicatorStyle

    /**
     * 气球膨胀缩放指示器 (灵感源自 tommybuonomo/dotsindicator 的 BalloonDotsIndicator)
     *
     * 选中项如气球般放大，拖动时跟随手势平滑缩放。
     */
    data class Balloon(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.45f),
        val dotSize: Dp = 6.dp,
        val balloonFactor: Float = 1.45f,
        val spacing: Dp = 6.dp
    ) : BannerIndicatorStyle

    /** 经典平滑圆点指示器 */
    data class Dots(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.45f),
        val dotSize: Dp = 6.dp,
        val spacing: Dp = 6.dp
    ) : BannerIndicatorStyle

    /** 小红书 / 微博风格数字药丸胶囊角标 (如 1/3) */
    data class PillNumber(
        val backgroundColor: Color = Color.Black.copy(alpha = 0.45f),
        val textColor: Color = Color.White.copy(alpha = 0.92f)
    ) : BannerIndicatorStyle

    /** 极简进度线条条状指示器 */
    data class Lines(
        val activeColor: Color = RedBrandColor,
        val inactiveColor: Color = Color.White.copy(alpha = 0.35f),
        val lineWidth: Dp = 22.dp,
        val lineHeight: Dp = 2.5.dp,
        val spacing: Dp = 4.dp
    ) : BannerIndicatorStyle

    /** 不显示任何指示器 */
    data object None : BannerIndicatorStyle
}

/**
 * 计算环形无限轮播下的连续平滑偏移量 (确保 0f <= offset < pageCount)
 */
fun computeContinuousOffset(currentPage: Int, pageOffsetFraction: Float, pageCount: Int): Float {
    if (pageCount <= 0) return 0f
    val raw = (currentPage % pageCount) + pageOffsetFraction
    val mod = raw % pageCount
    return if (mod < 0f) mod + pageCount else mod
}

/**
 * 计算两个点在环形圆周上的最短距离
 */
fun circularDistance(a: Float, b: Float, pageCount: Int): Float {
    if (pageCount <= 1) return 0f
    val diff = abs(a - b) % pageCount
    return min(diff, pageCount - diff)
}

/**
 * 通用轮播图指示器宿主组件
 *
 * 根据传入的 [BannerIndicatorStyle] 策略自动渲染对应的指示器，并支持传入 [pageOffsetFraction]
 * 实现毫秒级的手势位移跟随与流体形变。
 *
 * ### 示例：
 * ```kotlin
 * BannerIndicatorHost(
 *     style = BannerIndicatorStyle.Worm(),
 *     pageCount = 4,
 *     currentPage = 0,
 *     pageOffsetFraction = 0.2f
 * )
 * ```
 *
 * @param style 指示器样式策略（如 [BannerIndicatorStyle.Worm], [BannerIndicatorStyle.Spring], [BannerIndicatorStyle.PillNumber] 等）
 * @param pageCount 轮播真实总页数（小于等于 1 时自动隐藏）
 * @param currentPage 当前选中的真实索引
 * @param pageOffsetFraction 当前滑动的偏移百分比（-0.5f ~ 0.5f），用于驱动平滑物理动画
 * @param modifier 外部修饰符
 */
@Composable
fun BannerIndicatorHost(
    style: BannerIndicatorStyle,
    pageCount: Int,
    currentPage: Int,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1 || style is BannerIndicatorStyle.None) return

    Box(modifier = modifier) {
        when (style) {
            is BannerIndicatorStyle.Worm -> {
                WormBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            is BannerIndicatorStyle.Spring -> {
                SpringBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            is BannerIndicatorStyle.Shift -> {
                ShiftBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            is BannerIndicatorStyle.Balloon -> {
                BalloonBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            is BannerIndicatorStyle.Dots -> {
                DotsBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            is BannerIndicatorStyle.PillNumber -> {
                PillNumberBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style
                )
            }
            is BannerIndicatorStyle.Lines -> {
                LinesBannerIndicator(
                    pageCount = pageCount,
                    currentPage = currentPage,
                    style = style,
                    pageOffsetFraction = pageOffsetFraction
                )
            }
            BannerIndicatorStyle.None -> Unit
        }
    }
}

/**
 * 蠕动伸缩胶囊指示器实现 (Worm)
 */
@Composable
fun WormBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Worm,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)
    val stepDp = style.dotSize + style.spacing

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        // 1. 底层静态背景圆点
        Row(
            horizontalArrangement = Arrangement.spacedBy(style.spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) {
                Box(
                    modifier = Modifier
                        .size(style.dotSize)
                        .clip(CircleShape)
                        .background(style.inactiveColor)
                )
            }
        }

        // 2. 表层蠕动弹性胶囊
        val leftIndex = continuousOffset.toInt()
        val fraction = continuousOffset - leftIndex

        if (leftIndex < pageCount - 1) {
            val baseLeftDp = stepDp * leftIndex.toFloat()
            val wormLeftDp: Dp
            val wormWidthDp: Dp

            if (fraction <= 0.5f) {
                val headOffset = stepDp * (fraction * 2f)
                wormLeftDp = baseLeftDp
                wormWidthDp = style.dotSize + headOffset
            } else {
                val tailOffset = stepDp * ((fraction - 0.5f) * 2f)
                wormLeftDp = baseLeftDp + tailOffset
                wormWidthDp = (style.dotSize + stepDp) - tailOffset
            }

            Box(
                modifier = Modifier
                    .offset(x = wormLeftDp)
                    .height(style.dotSize)
                    .width(wormWidthDp)
                    .clip(CircleShape)
                    .background(style.activeColor)
            )
        } else {
            // 末尾与首项循环过渡边界 (N-1 -> 0)
            val lastDotLeftDp = stepDp * (pageCount - 1).toFloat()
            if (fraction <= 0.5f) {
                val progress = (fraction * 2f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = lastDotLeftDp)
                        .size(style.dotSize)
                        .graphicsLayer { alpha = 1f - progress }
                        .clip(CircleShape)
                        .background(style.activeColor)
                )
            } else {
                val progress = ((fraction - 0.5f) * 2f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = 0.dp)
                        .size(style.dotSize)
                        .graphicsLayer { alpha = progress }
                        .clip(CircleShape)
                        .background(style.activeColor)
                )
            }
        }
    }
}

/**
 * 弹性滑动圆点指示器实现 (Spring)
 */
@Composable
fun SpringBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Spring,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)
    val stepDp = style.dotSize + style.spacing

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        // 1. 底层静态背景圆点
        Row(
            horizontalArrangement = Arrangement.spacedBy(style.spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) {
                Box(
                    modifier = Modifier
                        .size(style.dotSize)
                        .clip(CircleShape)
                        .background(style.inactiveColor)
                )
            }
        }

        // 2. 表层平滑滑动的悬浮指示器
        val leftIndex = continuousOffset.toInt()
        val fraction = continuousOffset - leftIndex

        if (leftIndex < pageCount - 1) {
            val xDp = stepDp * continuousOffset
            Box(
                modifier = Modifier
                    .offset(x = xDp)
                    .size(style.selectorSize)
                    .clip(CircleShape)
                    .background(style.activeColor)
            )
        } else {
            // 环形边界跨越 (N-1 -> 0)
            val lastDotLeftDp = stepDp * (pageCount - 1).toFloat()
            if (fraction <= 0.5f) {
                val progress = (fraction * 2f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = lastDotLeftDp)
                        .size(style.selectorSize)
                        .graphicsLayer { alpha = 1f - progress }
                        .clip(CircleShape)
                        .background(style.activeColor)
                )
            } else {
                val progress = ((fraction - 0.5f) * 2f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = 0.dp)
                        .size(style.selectorSize)
                        .graphicsLayer { alpha = progress }
                        .clip(CircleShape)
                        .background(style.activeColor)
                )
            }
        }
    }
}

/**
 * 动态宽度拉伸指示器实现 (Shift)
 */
@Composable
fun ShiftBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Shift,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val dist = circularDistance(i.toFloat(), continuousOffset, pageCount).coerceIn(0f, 1f)
            val factor = 1f - dist
            val width = style.dotSize + (style.shiftFactor - 1f) * style.dotSize * factor
            val color = lerp(style.inactiveColor, style.activeColor, factor)

            Box(
                modifier = Modifier
                    .height(style.dotSize)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * 气球膨胀缩放指示器实现 (Balloon)
 */
@Composable
fun BalloonBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Balloon,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val dist = circularDistance(i.toFloat(), continuousOffset, pageCount).coerceIn(0f, 1f)
            val factor = 1f - dist
            val scale = 1f + (style.balloonFactor - 1f) * factor
            val color = lerp(style.inactiveColor, style.activeColor, factor)

            Box(
                modifier = Modifier
                    .size(style.dotSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * 经典平滑圆点指示器实现 (Dots)
 */
@Composable
fun DotsBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Dots,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val dist = circularDistance(i.toFloat(), continuousOffset, pageCount).coerceIn(0f, 1f)
            val factor = 1f - dist
            val color = lerp(style.inactiveColor, style.activeColor, factor)
            val scale = 1f + 0.20f * factor

            Box(
                modifier = Modifier
                    .size(style.dotSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * 经典圆点指示器便捷别名组件 (兼容 dotsindicator 语义)
 */
@Composable
fun DotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    style: BannerIndicatorStyle = BannerIndicatorStyle.Dots(),
    pageOffsetFraction: Float = 0f
) {
    BannerIndicatorHost(
        style = style,
        pageCount = pageCount,
        currentPage = currentPage,
        pageOffsetFraction = pageOffsetFraction,
        modifier = modifier
    )
}

/**
 * 小红书风格数字药丸胶囊指示器实现
 */
@Composable
fun PillNumberBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.PillNumber,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = style.backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = "${currentPage + 1}/$pageCount",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = style.textColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

/**
 * 极简线条进度指示器实现
 */
@Composable
fun LinesBannerIndicator(
    pageCount: Int,
    currentPage: Int,
    style: BannerIndicatorStyle.Lines,
    pageOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val continuousOffset = computeContinuousOffset(currentPage, pageOffsetFraction, pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val dist = circularDistance(i.toFloat(), continuousOffset, pageCount).coerceIn(0f, 1f)
            val factor = 1f - dist
            val color = lerp(style.inactiveColor, style.activeColor, factor)

            Box(
                modifier = Modifier
                    .width(style.lineWidth)
                    .height(style.lineHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}
