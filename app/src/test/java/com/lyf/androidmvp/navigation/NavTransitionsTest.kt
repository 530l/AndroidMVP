package com.lyf.androidmvp.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavTransitionsTest {

    @Test
    fun iosTransitionConstants_matchAppleStandardSpecs() {
        assertEquals("iOS 黄金转场时间应为 400ms", 400, IOS_NAV_DURATION_MS)
        assertEquals("底层视差移动比例应为 25%", 0.25f, IOS_PARALLAX_FACTOR, 0.001f)
        assertEquals("底层视差暗化透明度应为 0.85f", 0.85f, IOS_PARALLAX_ALPHA, 0.001f)
        assertNotNull("iOS 缓动曲线不能为 null", IosNavEasing)
    }

    @Test
    fun transitionSpecs_instantiateSuccessfully() {
        val pushSpec = iosPushTransitionSpec<AppDestination>()
        val popSpec = iosPopTransitionSpec<AppDestination>()
        val predictiveSpec = iosPredictivePopTransitionSpec<AppDestination>()

        assertNotNull("Push 转场规则构建器不能为 null", pushSpec)
        assertNotNull("Pop 转场规则构建器不能为 null", popSpec)
        assertNotNull("Predictive Pop 规则构建器不能为 null", predictiveSpec)
    }
}
