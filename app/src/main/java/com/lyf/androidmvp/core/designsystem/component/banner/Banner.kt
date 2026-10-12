package com.lyf.androidmvp.core.designsystem.component.banner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * 商业级 Jetpack Compose 通用轮播图底座组件 (Slot API)
 *
 * 核心特性 (吸收并升华 ComposeViews 优秀架构)：
 * 1. **虚拟大页数无限轮播**：正反向自由无限滑动，无页码截断；
 * 2. **手势智能感知**：按下 (Press) 或拖动 (Drag) 瞬间智能暂停自动播放，松手后重新启动倒计时；
 * 3. **画廊缩放视差** (`galleryEffect = true`)：当前居中卡片 1.0x 放大，两侧窥视卡片平滑过渡；
 * 4. **任意组件 Slot 插槽式内容定制**：完全解耦业务，支持商品卡片、快讯滚动、自定义 UI；
 * 5. **实时位移指示器插槽**：向指示器实时提供连续手势位移率 [pageOffsetFraction]。
 *
 * ---
 *
 * ### 常用代码案例：
 *
 * #### 案例 1：自定义任意卡片 Slot (如新闻快讯或商品卡片)
 * ```kotlin
 * Banner(
 *     pageCount = productList.size,
 *     modifier = Modifier.fillMaxWidth().height(180.dp),
 *     indicator = { actualIndex, totalCount, offsetFraction ->
 *         BannerIndicatorHost(
 *             style = BannerIndicatorStyle.Worm(),
 *             pageCount = totalCount,
 *             currentPage = actualIndex,
 *             pageOffsetFraction = offsetFraction,
 *             modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
 *         )
 *     }
 * ) { page, actualIndex ->
 *     val product = productList[actualIndex]
 *     ProductCard(product = product)
 * }
 * ```
 *
 * #### 案例 2：开启画廊窥视视差模式
 * ```kotlin
 * Banner(
 *     pageCount = cards.size,
 *     galleryEffect = true,
 *     contentPadding = PaddingValues(horizontal = 24.dp),
 *     pageSpacing = 12.dp,
 *     minScale = 0.90f,
 *     minAlpha = 0.80f
 * ) { _, actualIndex ->
 *     CustomCard(item = cards[actualIndex])
 * }
 * ```
 *
 * @param pageCount 轮播图真实数据总数
 * @param modifier 外部修饰符
 * @param autoScroll 是否开启自动轮播 (默认 true)
 * @param autoScrollIntervalMillis 自动轮播时间间隔 (毫秒，默认 3500ms)
 * @param isInfinite 是否启用无限循环轮播 (默认 true)
 * @param contentPadding 轮播内容内边距 (可用于画廊两端窥视留白)
 * @param pageSpacing 页面之间的水平间隔
 * @param galleryEffect 是否开启居中放大、两侧缩小的画廊视差动效 (默认 false)
 * @param minScale 画廊模式下两侧未选中卡片的最小缩放比例 (默认 0.92f)
 * @param minAlpha 画廊模式下两侧未选中卡片的最小透明度 (默认 0.85f)
 * @param userScrollEnabled 是否允许用户手动滑动 (默认 true)
 * @param indicator 指示器插槽，接收 (actualIndex, totalCount, pageOffsetFraction)
 * @param content 单页卡片内容插槽，接收 (virtualPage, actualIndex)
 */
@Composable
fun Banner(
    pageCount: Int,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = true,
    autoScrollIntervalMillis: Long = 3500L,
    isInfinite: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSpacing: Dp = 0.dp,
    galleryEffect: Boolean = false,
    minScale: Float = 0.92f,
    minAlpha: Float = 0.85f,
    userScrollEnabled: Boolean = true,
    indicator: (@Composable (actualIndex: Int, pageCount: Int, pageOffsetFraction: Float) -> Unit)? = null,
    content: @Composable (page: Int, actualIndex: Int) -> Unit
) {
    if (pageCount <= 0) return

    // 1. 计算虚拟大页数实现无限轮播
    val virtualPageCount = if (isInfinite && pageCount > 1) pageCount * 1000 else pageCount
    val initialPage = if (isInfinite && pageCount > 1) {
        val middle = virtualPageCount / 2
        middle - (middle % pageCount)
    } else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualPageCount }
    )

    // 2. 手势状态智能感知 (按住或拖拽均暂停轮播)
    var isUserInteracting by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.interactionSource) {
        pagerState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press, is DragInteraction.Start -> isUserInteracting = true
                is PressInteraction.Release, is PressInteraction.Cancel,
                is DragInteraction.Stop, is DragInteraction.Cancel -> isUserInteracting = false
            }
        }
    }

    // 3. 自动轮播协程驱动
    LaunchedEffect(autoScroll, isUserInteracting, pageCount, autoScrollIntervalMillis) {
        if (autoScroll && !isUserInteracting && pageCount > 1) {
            while (true) {
                delay(autoScrollIntervalMillis)
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val currentActualIndex = pagerState.currentPage % pageCount

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = contentPadding,
            pageSpacing = pageSpacing,
            userScrollEnabled = userScrollEnabled,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val actualIndex = page % pageCount

            if (galleryEffect) {
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue
                    .coerceIn(0f, 1f)
                val scale = 1f - (1f - minScale) * pageOffset
                val alpha = 1f - (1f - minAlpha) * pageOffset

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    content(page, actualIndex)
                }
            } else {
                content(page, actualIndex)
            }
        }

        if (indicator != null && pageCount > 1) {
            indicator(
                currentActualIndex,
                pageCount,
                pagerState.currentPageOffsetFraction
            )
        }
    }
}

/**
 * 兼容双参数指示器签名的重载函数
 */
@Composable
fun Banner(
    pageCount: Int,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = true,
    autoScrollIntervalMillis: Long = 3500L,
    isInfinite: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSpacing: Dp = 0.dp,
    galleryEffect: Boolean = false,
    minScale: Float = 0.92f,
    minAlpha: Float = 0.85f,
    userScrollEnabled: Boolean = true,
    indicator: (@Composable (actualIndex: Int, pageCount: Int) -> Unit)?,
    content: @Composable (page: Int, actualIndex: Int) -> Unit
) {
    val wrappedIndicator: (@Composable (Int, Int, Float) -> Unit)? = if (indicator != null) {
        { actualIndex, count, _ -> indicator(actualIndex, count) }
    } else null

    Banner(
        pageCount = pageCount,
        modifier = modifier,
        autoScroll = autoScroll,
        autoScrollIntervalMillis = autoScrollIntervalMillis,
        isInfinite = isInfinite,
        contentPadding = contentPadding,
        pageSpacing = pageSpacing,
        galleryEffect = galleryEffect,
        minScale = minScale,
        minAlpha = minAlpha,
        userScrollEnabled = userScrollEnabled,
        indicator = wrappedIndicator,
        content = content
    )
}
