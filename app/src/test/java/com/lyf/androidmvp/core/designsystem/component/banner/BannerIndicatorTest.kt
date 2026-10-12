package com.lyf.androidmvp.core.designsystem.component.banner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BannerIndicatorTest {

    @Test
    fun computeContinuousOffset_withinBounds_returnsAccurateOffset() {
        // 初始页面 0，无偏移
        assertEquals(0f, computeContinuousOffset(0, 0f, 3), 0.0001f)

        // 页面 0，向前滑动 30%
        assertEquals(0.3f, computeContinuousOffset(0, 0.3f, 3), 0.0001f)

        // 页面 1，向后滑动 30% (即 offsetFraction = -0.3f)，连续位移应平滑对应 0.7f
        assertEquals(0.7f, computeContinuousOffset(1, -0.3f, 3), 0.0001f)

        // 页面 1，完全就位
        assertEquals(1.0f, computeContinuousOffset(1, 0f, 3), 0.0001f)

        // 页面 2，向前滑动 40%
        assertEquals(2.4f, computeContinuousOffset(2, 0.4f, 3), 0.0001f)
    }

    @Test
    fun computeContinuousOffset_infiniteLoopBoundary_wrapsSmoothly() {
        // 页面 2 (最后一页)，向前滑动 40% (即朝第 0 页滑)
        assertEquals(2.4f, computeContinuousOffset(2, 0.4f, 3), 0.0001f)

        // 跨过 0.5 阈值后：virtualPage 变为 3 (对应真实 index 0)，offsetFraction 变为 -0.5f
        // 期望连续位置仍为 2.5f
        assertEquals(2.5f, computeContinuousOffset(3, -0.5f, 3), 0.0001f)

        // 从第 0 页向后滑动 (手势反向) offsetFraction = -0.2f -> 应平滑映射到 2.8f
        assertEquals(2.8f, computeContinuousOffset(0, -0.2f, 3), 0.0001f)

        // 虚拟大页数场景 (例如 index 3000 % 3 = 0)
        assertEquals(0.25f, computeContinuousOffset(3000, 0.25f, 3), 0.0001f)
        assertEquals(2.75f, computeContinuousOffset(3000, -0.25f, 3), 0.0001f)
    }

    @Test
    fun circularDistance_calculatesShortestDistanceOnRing() {
        val count = 3

        // 同一位置距离为 0
        assertEquals(0f, circularDistance(0f, 0f, count), 0.0001f)

        // 0 与 1 距离为 1
        assertEquals(1f, circularDistance(0f, 1f, count), 0.0001f)

        // 0 与 2 在环形上距离为 1 (0 -> 2 是逆时针 1 步)
        assertEquals(1f, circularDistance(0f, 2f, count), 0.0001f)

        // 0 与 2.8 距离为 0.2
        assertEquals(0.2f, circularDistance(0f, 2.8f, count), 0.0001f)

        // 1 与 2.4 距离为 1.4 (正向 1.4, 反向 3 - 1.4 = 1.6, 取最小值 1.4)
        assertEquals(1.4f, circularDistance(1f, 2.4f, count), 0.0001f)
    }

    @Test
    fun indicatorStyles_instantiateWithDefaults() {
        val worm = BannerIndicatorStyle.Worm()
        assertTrue(worm.dotSize.value > 0f)
        assertTrue(worm.spacing.value > 0f)

        val spring = BannerIndicatorStyle.Spring()
        assertEquals(spring.dotSize, spring.selectorSize)

        val shift = BannerIndicatorStyle.Shift()
        assertTrue(shift.shiftFactor > 1f)

        val balloon = BannerIndicatorStyle.Balloon()
        assertTrue(balloon.balloonFactor > 1f)

        val dots = BannerIndicatorStyle.Dots()
        assertTrue(dots.dotSize.value > 0f)

        val pill = BannerIndicatorStyle.PillNumber()
        assertTrue(pill.backgroundColor.alpha > 0f)

        val lines = BannerIndicatorStyle.Lines()
        assertTrue(lines.lineWidth.value > lines.lineHeight.value)
    }
}
