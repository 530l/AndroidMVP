package com.lyf.androidmvp.core.designsystem.component.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * 轮播图数据契约协议
 *
 * 任何业务实体（如 BannerItem, Article, ActivityPromotion 等）只需实现该接口，
 * 即可直接传入 [ImageBanner] 中进行轮播展示。
 *
 * ```kotlin
 * data class HomeBannerVo(
 *     override val id: Long,
 *     override val title: String,
 *     override val imageUrl: String,
 *     override val linkUrl: String = ""
 * ) : BannerData
 * ```
 */
interface BannerData {
    val id: Long
    val title: String
    val imageUrl: String
    val linkUrl: String get() = ""
}

/**
 * 商业级 Jetpack Compose 开箱即用图片轮播图组件 (ImageBanner)
 *
 * 组合底座 [Banner] 与指示器宿主 [BannerIndicatorHost]，提供符合现代移动端体验的图片轮播方案。
 *
 * ### 核心特性：
 * 1. **无限无缝循环**：基于虚拟大页数实现真实双向无限轮播，无跳变瑕疵；
 * 2. **手势智能感知**：用户手指按下（Press）或拖拽（Drag）瞬间暂停自动轮播，松手后重新启动倒计时；
 * 3. **抗色差渐变蒙层**：卡片底部自带深色半透明渐变层，确保任何白色或复杂浅色背景图上的文字均清晰可读；
 * 4. **实时物理流体指示器**：支持来自 dotsindicator 的 Worm（蠕动）、Spring（弹性）、Shift（拉伸）、Balloon（气球）等多种手势实时同步指示器；
 * 5. **画廊窥视视差**：支持一键开启两侧卡片露头缩放视差（`galleryEffect = true`）。
 *
 * ---
 *
 * ### 常用代码案例：
 *
 * #### 案例 1：最简标准用法（默认双阶段蠕虫指示器）
 * ```kotlin
 * ImageBanner(
 *     items = state.banners,
 *     onItemClick = { banner ->
 *         navController.navigate("detail/${banner.id}")
 *     },
 *     modifier = Modifier.padding(horizontal = 16.dp)
 * )
 * ```
 *
 * #### 案例 2：小红书 / 电商风格数字胶囊角标 (如 1/3)
 * ```kotlin
 * ImageBanner(
 *     items = state.banners,
 *     onItemClick = { banner -> ... },
 *     indicatorStyle = BannerIndicatorStyle.PillNumber(),
 *     indicatorAlignment = Alignment.BottomEnd,
 *     aspectRatio = 1.8f
 * )
 * ```
 *
 * #### 案例 3：画廊卡片视差窥视模式（两端露头 + 居中放大）
 * ```kotlin
 * ImageBanner(
 *     items = state.banners,
 *     onItemClick = { banner -> ... },
 *     galleryEffect = true,
 *     contentPadding = PaddingValues(horizontal = 24.dp),
 *     pageSpacing = 12.dp,
 *     indicatorStyle = BannerIndicatorStyle.Spring(),
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * #### 案例 4：自定义自动轮播间隔与动态宽度拉伸指示器
 * ```kotlin
 * ImageBanner(
 *     items = state.banners,
 *     onItemClick = { banner -> ... },
 *     autoScrollIntervalMillis = 5000L,
 *     indicatorStyle = BannerIndicatorStyle.Shift(shiftFactor = 3f)
 * )
 * ```
 *
 * @param T 实现 [BannerData] 接口的业务数据模型类型
 * @param items 轮播图数据列表
 * @param onItemClick 点击卡片时的事件回调
 * @param modifier 外部修饰符
 * @param aspectRatio 图片卡片的长宽比（默认 2.1f）
 * @param shape 卡片圆角轮廓（默认 14.dp 圆角）
 * @param autoScroll 是否开启自动轮播（默认 true）
 * @param autoScrollIntervalMillis 自动轮播时间间隔（毫秒，默认 3500ms）
 * @param galleryEffect 是否开启画廊两侧露头缩放视差动效（默认 false）
 * @param contentPadding 轮播内容内边距（在开启画廊时默认两边预留 20.dp）
 * @param pageSpacing 页面之间的水平间隔（在开启画廊时默认 10.dp）
 * @param indicatorStyle 指示器样式策略（默认 [BannerIndicatorStyle.Worm] 物理流体蠕虫）
 * @param indicatorAlignment 指示器在卡片中的对齐方式（默认 [Alignment.BottomEnd] 右下角）
 */
@Composable
fun <T : BannerData> ImageBanner(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2.1f,
    shape: Shape = RoundedCornerShape(14.dp),
    autoScroll: Boolean = true,
    autoScrollIntervalMillis: Long = 3500L,
    galleryEffect: Boolean = false,
    contentPadding: PaddingValues = if (galleryEffect) PaddingValues(horizontal = 20.dp) else PaddingValues(0.dp),
    pageSpacing: Dp = if (galleryEffect) 10.dp else 0.dp,
    indicatorStyle: BannerIndicatorStyle = BannerIndicatorStyle.Worm(),
    indicatorAlignment: Alignment = Alignment.BottomEnd
) {
    if (items.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .aspectRatio(aspectRatio)
    ) {
        Banner(
            pageCount = items.size,
            autoScroll = autoScroll,
            autoScrollIntervalMillis = autoScrollIntervalMillis,
            galleryEffect = galleryEffect,
            contentPadding = contentPadding,
            pageSpacing = pageSpacing,
            modifier = Modifier.fillMaxSize(),
            indicator = { actualIndex, totalCount, offsetFraction ->
                val indicatorPadding = if (indicatorStyle is BannerIndicatorStyle.PillNumber) {
                    Modifier.padding(end = 12.dp, bottom = 8.dp)
                } else {
                    Modifier.padding(end = 14.dp, bottom = 10.dp)
                }

                BannerIndicatorHost(
                    style = indicatorStyle,
                    pageCount = totalCount,
                    currentPage = actualIndex,
                    pageOffsetFraction = offsetFraction,
                    modifier = Modifier
                        .align(indicatorAlignment)
                        .then(indicatorPadding)
                )
            }
        ) { _, actualIndex ->
            val item = items[actualIndex]
            ImageBannerCard(
                item = item,
                shape = shape,
                hasIndicatorOnEnd = indicatorStyle !is BannerIndicatorStyle.None,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * 扁平化单项图片卡片组件 (消除金字塔嵌套)
 */
@Composable
private fun <T : BannerData> ImageBannerCard(
    item: T,
    shape: Shape,
    hasIndicatorOnEnd: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        // 1. 高清图片加载层
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. 底部暗色渐变蒙层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        // 3. 标题展示
        if (item.title.isNotBlank()) {
            val endPadding = if (hasIndicatorOnEnd) 80.dp else 16.dp
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = endPadding, bottom = 10.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// IDE 可视化实时预览 (Preview Demos)
// -------------------------------------------------------------------------

private data class PreviewBannerItem(
    override val id: Long,
    override val title: String,
    override val imageUrl: String,
    override val linkUrl: String = ""
) : BannerData

private val previewBannerMockList = listOf(
    PreviewBannerItem(1, "Google I/O 开发者大会精华速递", ""),
    PreviewBannerItem(2, "Jetpack Compose 现代化架构实战", ""),
    PreviewBannerItem(3, "Kotlin Multiplatform 跨平台开发探秘", "")
)

@Preview(name = "标准用法 (Worm 蠕动指示器)", showBackground = true)
@Composable
private fun ImageBannerDefaultPreview() {
    MaterialTheme {
        ImageBanner(
            items = previewBannerMockList,
            onItemClick = {},
            indicatorStyle = BannerIndicatorStyle.Worm(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "画廊窥视视差 (Spring 弹性指示器)", showBackground = true)
@Composable
private fun ImageBannerGalleryPreview() {
    MaterialTheme {
        ImageBanner(
            items = previewBannerMockList,
            onItemClick = {},
            galleryEffect = true,
            indicatorStyle = BannerIndicatorStyle.Spring(),
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(name = "电商/小红书角标 (PillNumber 数字胶囊)", showBackground = true)
@Composable
private fun ImageBannerPillNumberPreview() {
    MaterialTheme {
        ImageBanner(
            items = previewBannerMockList,
            onItemClick = {},
            indicatorStyle = BannerIndicatorStyle.PillNumber(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
