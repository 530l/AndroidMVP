package com.lyf.androidmvp.feature.profile.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyf.androidmvp.feature.main.RedBrandColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 笔记/收藏/赞过 动态动画 TabLayout (严格靠左排列，紧凑无 90dp 强制宽度，高光平滑滑移药丸指示器)
 */
@Composable
fun ProfileTabLayout(
    pagerState: PagerState,
    notesCount: Int = 4,
    favoritesCount: Int = 0,
    likesCount: Int = 18,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf(
        "笔记 $notesCount",
        "收藏 $favoritesCount",
        "赞过 $likesCount"
    )
    val density = LocalDensity.current

    val tabCenters = remember { mutableStateMapOf<Int, Float>() }
    val currentPage = pagerState.currentPage

    val targetCenterPx = tabCenters[currentPage] ?: 0f
    val animatedCenterDp by animateDpAsState(
        targetValue = with(density) { targetCenterPx.toDp() },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "profile_tab_indicator_anim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = currentPage == index
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInParent()
                                val centerX = position.x + coordinates.size.width / 2f
                                tabCenters[index] = centerX
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = if (isSelected) 16.sp else 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 平滑滑动的指示器药丸 (居中在当前选中 Tab 下方)
            if (animatedCenterDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset {
                            val pillHalfWidthPx = with(density) { 14.dp.toPx() }
                            val xPx = with(density) { animatedCenterDp.toPx() } - pillHalfWidthPx
                            IntOffset(x = xPx.roundToInt(), y = 0)
                        }
                        .width(28.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(RedBrandColor)
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }
}
