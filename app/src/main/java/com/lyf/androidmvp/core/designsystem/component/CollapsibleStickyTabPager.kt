package com.lyf.androidmvp.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 商业级折叠头部与吸顶 Tab 联动滚动容器组件
 * 对应传统 Android View 时代的 CoordinatorLayout + AppBarLayout(scroll|exitUntilCollapsed) + TabLayout + ViewPager2。
 *
 * 核心特性与冲突治理：
 * 1. 采用原生精确定位 Layout，Header 在 [0, -headerHeight] 滑移，Tab 在 y >= 0 吸顶，Pager 占满剩余视口；
 * 2. 嵌套滚动闭环：上滑在 onPreScroll 中折叠头部，下滑在列表到顶后通过 onPostScroll 精确恢复展开；
 * 3. 惯性冲量衔接：支持 onPreFling 与 onPostFling，列表快速下拉至顶部时顺势平滑弹开折叠头部；
 * 4. Header & Tab 手势穿透：在头部与吸顶 Tab 上附加拖拽监听，直接拖动 Tab 或头部亦可 100% 顺畅展开/收起，杜绝假死卡顿；
 * 5. 支持实时暴露 collapseFraction (0f 展开 -> 1f 折叠)；
 * 6. 支持右下角悬浮按钮 (FAB)。
 */
@Composable
fun CollapsibleStickyTabPager(
    modifier: Modifier = Modifier,
    headerContent: @Composable (collapseFraction: Float) -> Unit,
    stickyTabContent: @Composable () -> Unit,
    pagerContent: @Composable () -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null
) {
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }
    val coroutineScope = rememberCoroutineScope()
    var headerHeightPx by rememberSaveable { mutableFloatStateOf(0f) }
    var headerOffsetPx by rememberSaveable { mutableFloatStateOf(0f) }

    // 直接在 Header 或 StickyTab 区域上下拖拽时的平滑响应与下拉刷新联动修饰符
    val headerDragModifier = Modifier.draggable(
        state = rememberDraggableState { delta ->
            // 1. 先询问父级 (如最外层的 PullToRefreshBox) 是否在 preScroll 中消费 (例如推回拉出的刷新指示器)
            val preConsumed = nestedScrollDispatcher.dispatchPreScroll(
                available = Offset(0f, delta),
                source = NestedScrollSource.UserInput
            )
            val remainingAfterPre = delta - preConsumed.y

            if (remainingAfterPre < 0f) {
                // 手指向上推 (delta < 0)：优先折叠收起头部
                if (headerHeightPx > 0f && headerOffsetPx > -headerHeightPx) {
                    val newOffset = (headerOffsetPx + remainingAfterPre).coerceIn(-headerHeightPx, 0f)
                    val consumedByHeader = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    val remaining = remainingAfterPre - consumedByHeader
                    if (remaining != 0f) {
                        nestedScrollDispatcher.dispatchPostScroll(
                            consumed = Offset(0f, consumedByHeader),
                            available = Offset(0f, remaining),
                            source = NestedScrollSource.UserInput
                        )
                    }
                } else {
                    // 头部已完全折叠，多余的上推位移冒泡给嵌套容器
                    nestedScrollDispatcher.dispatchPostScroll(
                        consumed = Offset.Zero,
                        available = Offset(0f, remainingAfterPre),
                        source = NestedScrollSource.UserInput
                    )
                }
            } else if (remainingAfterPre > 0f) {
                // 手指向下拉动 (delta > 0)：
                if (headerHeightPx > 0f && headerOffsetPx < 0f) {
                    // 头部处于折叠/吸顶状态：优先将头部展开还原
                    val newOffset = (headerOffsetPx + remainingAfterPre).coerceIn(-headerHeightPx, 0f)
                    val consumedByHeader = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    val remaining = remainingAfterPre - consumedByHeader
                    if (remaining > 0f) {
                        // 头部已完全展开至 0f，单次手势多余的下拉位移顺畅透传给 PullToRefreshBox 触发下拉刷新！
                        nestedScrollDispatcher.dispatchPostScroll(
                            consumed = Offset(0f, consumedByHeader),
                            available = Offset(0f, remaining),
                            source = NestedScrollSource.UserInput
                        )
                    }
                } else {
                    // 头部已处于完全展开状态 (headerOffsetPx == 0f)：
                    // 直接将整个下拉位移冒泡派发给外层 PullToRefreshBox，拉出下拉刷新指示器！
                    nestedScrollDispatcher.dispatchPostScroll(
                        consumed = Offset.Zero,
                        available = Offset(0f, remainingAfterPre),
                        source = NestedScrollSource.UserInput
                    )
                }
            }
        },
        orientation = Orientation.Vertical,
        onDragStopped = { velocity ->
            coroutineScope.launch {
                val preFlingConsumed = nestedScrollDispatcher.dispatchPreFling(
                    available = Velocity(0f, velocity)
                )
                val remainingVelocity = velocity - preFlingConsumed.y

                if (remainingVelocity > 0f) {
                    // 向下释放
                    if (headerOffsetPx < 0f) {
                        // 处于折叠中，平滑弹开展开头部
                        val anim = Animatable(headerOffsetPx)
                        anim.animateTo(0f, initialVelocity = remainingVelocity) {
                            headerOffsetPx = value
                        }
                    } else {
                        // 头部已处于展开状态：将释放惯性派发给外层 PullToRefreshBox 触发刷新
                        nestedScrollDispatcher.dispatchPostFling(
                            consumed = Velocity.Zero,
                            available = Velocity(0f, remainingVelocity)
                        )
                    }
                } else if (remainingVelocity < 0f) {
                    // 向上释放：折叠头部
                    if (headerHeightPx > 0f && headerOffsetPx > -headerHeightPx) {
                        val anim = Animatable(headerOffsetPx)
                        anim.animateTo(-headerHeightPx, initialVelocity = remainingVelocity) {
                            headerOffsetPx = value
                        }
                    }
                }
            }
        }
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // 手指向上推 (delta < 0)：优先折叠收起头部
                if (delta < 0 && headerHeightPx > 0f && headerOffsetPx > -headerHeightPx) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-headerHeightPx, 0f)
                    val consumed = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                // 子列表已滑到最顶部，继续下拉 (delta > 0)：展开头部
                if (delta > 0 && headerHeightPx > 0f && headerOffsetPx < 0f) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-headerHeightPx, 0f)
                    val consumedY = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // 向上快滑猛推时，若头部未完全折叠，顺势折叠完毕
                if (available.y < 0f && headerHeightPx > 0f && headerOffsetPx > -headerHeightPx) {
                    val anim = Animatable(headerOffsetPx)
                    anim.animateTo(
                        targetValue = -headerHeightPx,
                        initialVelocity = available.y
                    ) {
                        headerOffsetPx = value
                    }
                    return Velocity(0f, available.y)
                }
                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // 快速向下滑动至列表顶部时，带有剩余惯性冲量，顺势平滑弹开头部
                if (available.y > 0f && headerHeightPx > 0f && headerOffsetPx < 0f) {
                    val anim = Animatable(headerOffsetPx)
                    anim.animateTo(
                        targetValue = 0f,
                        initialVelocity = available.y
                    ) {
                        headerOffsetPx = value
                    }
                    return Velocity(0f, available.y)
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val collapseFraction = if (headerHeightPx > 0f) {
        (-headerOffsetPx / headerHeightPx).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        Layout(
            content = {
                // slot 0: 可折叠 Header (支持直接手势拖动)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(headerDragModifier)
                ) {
                    headerContent(collapseFraction)
                }
                // slot 1: 吸顶 Tab (支持直接手势拖动)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(headerDragModifier)
                ) {
                    stickyTabContent()
                }
                // slot 2: 内容 Pager
                Box(modifier = Modifier.fillMaxWidth()) {
                    pagerContent()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection, nestedScrollDispatcher)
        ) { measurables, constraints ->
            val screenWidth = constraints.maxWidth
            val screenHeight = constraints.maxHeight

            // 1. 测量 Header
            val headerPlaceable = measurables[0].measure(
                constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
            )
            val measuredHeaderHeight = headerPlaceable.height
            if (headerHeightPx != measuredHeaderHeight.toFloat()) {
                headerHeightPx = measuredHeaderHeight.toFloat()
            }

            // 2. 测量 吸顶 Tab 栏
            val tabPlaceable = measurables[1].measure(
                constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
            )
            val tabHeight = tabPlaceable.height

            // 3. 测量 Pager：高度严格等于 screenHeight - tabHeight
            val pagerHeight = (screenHeight - tabHeight).coerceAtLeast(0)
            val pagerPlaceable = measurables[2].measure(
                constraints.copy(minHeight = pagerHeight, maxHeight = pagerHeight)
            )

            layout(screenWidth, screenHeight) {
                val currentOffset = headerOffsetPx.coerceIn(-measuredHeaderHeight.toFloat(), 0f)

                // 1. Header 放置在 currentOffset (0 到 -headerHeight)
                headerPlaceable.placeRelative(0, currentOffset.roundToInt())

                // 2. Tab 放置在 Header 底部，吸顶在 0 (y >= 0)
                val tabY = (measuredHeaderHeight + currentOffset).roundToInt().coerceAtLeast(0)
                tabPlaceable.placeRelative(0, tabY)

                // 3. Pager 紧跟在 Tab 底部
                val pagerY = (measuredHeaderHeight + currentOffset + tabHeight).roundToInt().coerceAtLeast(tabHeight)
                pagerPlaceable.placeRelative(0, pagerY)
            }
        }

        // 浮动按钮
        if (floatingActionButton != null) {
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                floatingActionButton()
            }
        }
    }
}
