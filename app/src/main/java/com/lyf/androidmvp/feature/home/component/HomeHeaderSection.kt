package com.lyf.androidmvp.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lyf.androidmvp.core.designsystem.component.BannerItem
import com.lyf.androidmvp.core.designsystem.component.banner.BannerIndicatorStyle
import com.lyf.androidmvp.core.designsystem.component.banner.ImageBanner
import com.lyf.androidmvp.feature.home.HomeUiState

/**
 * 首页可折叠头部区域 (严密封装，负责平铺顶部各横向独立区块，彻底消灭外层金字塔嵌套)
 */
@Composable
fun HomeHeaderSection(
    state: HomeUiState,
    collapseFraction: Float,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onBannerClick: (BannerItem) -> Unit,
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .graphicsLayer {
                // 向上滑动折叠时，平滑减弱透明度
                alpha = (1f - collapseFraction * 0.75f).coerceIn(0.2f, 1f)
            }
    ) {
        HomeTopBar(
            onRefresh = onRefresh,
            onSettingsClick = onSettingsClick
        )

        HomeOfflineBanner(isOnline = state.isOnline)

        HomeSearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange
        )

        ImageBanner(
            items = defaultHomeBanners,
            onItemClick = onBannerClick,
            indicatorStyle = BannerIndicatorStyle.Shift(
                dotSize = 6.dp,
                shiftFactor = 3f,
                spacing = 5.dp
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        ExploreMenuGrid(
            onMenuItemClick = onMenuItemClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
    }
}

/**
 * 默认推荐横幅列表
 */
val defaultHomeBanners = listOf(
    BannerItem(
        id = 30,
        title = "全新 Android 官方架构规范与 Nav3 极速脚手架",
        imageUrl = "https://www.wanandroid.com/blogimgs/42da12d8-de56-4439-b40c-eab66c227a4b.png",
        linkUrl = "https://www.wanandroid.com/blog/show/3352"
    ),
    BannerItem(
        id = 6,
        title = "Jetpack Compose + Miuix 跨平台组件库实战",
        imageUrl = "https://www.wanandroid.com/blogimgs/62c1bd68-b5f3-4a3c-a649-7ca8c7dfabe6.png",
        linkUrl = "https://www.wanandroid.com/navi"
    ),
    BannerItem(
        id = 10,
        title = "一起来做个现代化的 App 吧",
        imageUrl = "https://www.wanandroid.com/blogimgs/50c115c2-cf6c-4802-aa7b-a4334de444cd.png",
        linkUrl = "https://www.wanandroid.com/blog/show/2"
    )
)
