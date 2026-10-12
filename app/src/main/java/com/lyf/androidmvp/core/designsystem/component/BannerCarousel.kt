package com.lyf.androidmvp.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.core.designsystem.component.banner.BannerData
import com.lyf.androidmvp.core.designsystem.component.banner.BannerIndicatorStyle
import com.lyf.androidmvp.core.designsystem.component.banner.ImageBanner

/**
 * 轮播图通用数据模型
 */
data class BannerItem(
    override val id: Long,
    override val title: String,
    override val imageUrl: String,
    override val linkUrl: String = ""
) : BannerData

/**
 * 商业级 Jetpack Compose 轮播图组件
 * 兼容原有调用的同时，底层无缝接入吸收 ComposeViews 精华的全新 [ImageBanner] 引擎。
 *
 * 特性：
 * 1. 0 冗余第三方库，纯原生 Compose 实现；
 * 2. 自动循环轮播定时器；
 * 3. 交互手势智能感知：按下 (Press) 或拖动 (Drag) 自动暂停，松手后重新启动倒计时；
 * 4. 默认配备现代蠕动伸缩胶囊动画指示器 (Worm Indicator) 或数字角标；
 * 5. 支持画廊窥视效果 (Gallery Effect)。
 */
@Composable
fun BannerCarousel(
    banners: List<BannerItem>,
    onBannerClick: (BannerItem) -> Unit,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = true,
    autoScrollIntervalMillis: Long = 3500L,
    aspectRatio: Float = 2.1f,
    cornerRadius: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    indicatorStyle: BannerIndicatorStyle = BannerIndicatorStyle.Worm(),
    galleryEffect: Boolean = false
) {
    ImageBanner(
        items = banners,
        onItemClick = onBannerClick,
        modifier = modifier,
        aspectRatio = aspectRatio,
        shape = shape,
        autoScroll = autoScroll,
        autoScrollIntervalMillis = autoScrollIntervalMillis,
        indicatorStyle = indicatorStyle,
        galleryEffect = galleryEffect
    )
}
