package com.lyf.androidmvp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

/**
 * iOS 经典导航转场曲线 (Apple Navigation Timing Curve)
 * 具备极佳的手感：初段响应迅速，末段极度柔顺平滑地减速停止 (Ease-Out)
 */
val IosNavEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

/**
 * iOS 导航动画时长（毫秒）
 * 经过大量 iOS 界面逐帧比对，400ms 是既灵动迅速又具备清晰视差纵深的黄金时长
 */
const val IOS_NAV_DURATION_MS = 400

/**
 * 视差偏移比例 (Parallax Ratio)
 * iOS 底层页面退居幕后或恢复时，仅移动屏幕宽度的 25%，带来立体的层级深度感
 */
const val IOS_PARALLAX_FACTOR = 0.25f

/**
 * 底层页面视差暗化透明度
 */
const val IOS_PARALLAX_ALPHA = 0.85f

/**
 * iOS 风格前进/打开新页面转场动画 (Push / Forward Transition)
 *
 * 视觉特征：
 * 1. 目标页面（新页面）：从屏幕右侧边缘 (100% 宽度处) 顺滑滑入至中央，处于上层 (ZIndex = 1f)
 * 2. 初始页面（旧页面）：向左微移 25% 产生视差 (Parallax)，并微弱暗化淡出 (Alpha -> 0.85f)
 */
fun <T : Any> iosPushTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val enterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        initialOffsetX = { fullWidth -> fullWidth }
    )
    val exitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        targetOffsetX = { fullWidth -> -(fullWidth * IOS_PARALLAX_FACTOR).toInt() }
    ) + fadeOut(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        targetAlpha = IOS_PARALLAX_ALPHA
    )

    (enterTransition togetherWith exitTransition).apply {
        targetContentZIndex = 1f
    }
}

/**
 * iOS 风格返回/关闭页面转场动画 (Pop / Backward Transition)
 *
 * 视觉特征：
 * 1. 初始页面（正在关闭的页面）：保持在上层覆盖，从中央平滑向右侧滑出至 100% 宽度处 (ZIndex = -1f 保障旧页在上)
 * 2. 目标页面（恢复的上一级页面）：从左侧 -25% 视差位置复位回中央，透明度从 0.85f 恢复至 1.0f
 */
fun <T : Any> iosPopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val enterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        initialOffsetX = { fullWidth -> -(fullWidth * IOS_PARALLAX_FACTOR).toInt() }
    ) + fadeIn(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        initialAlpha = IOS_PARALLAX_ALPHA
    )
    val exitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
        targetOffsetX = { fullWidth -> fullWidth }
    )

    (enterTransition togetherWith exitTransition).apply {
        // 让底层恢复的页面处于较低 Z 轴，关闭的页面覆盖在其上方滑出
        targetContentZIndex = -1f
    }
}

/**
 * iOS 风格预测性手势侧滑返回动画 (Predictive Pop Transition)
 * 配合 Android 14+ / 系统侧滑返回手势，保持完全一致的横向视差滑行动画
 */
fun <T : Any> iosPredictivePopTransitionSpec(): (AnimatedContentTransitionScope<Scene<T>>, Int) -> ContentTransform = { scope, _ ->
    with(scope) {
        val enterTransition = slideInHorizontally(
            animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
            initialOffsetX = { fullWidth -> -(fullWidth * IOS_PARALLAX_FACTOR).toInt() }
        ) + fadeIn(
            animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
            initialAlpha = IOS_PARALLAX_ALPHA
        )
        val exitTransition = slideOutHorizontally(
            animationSpec = tween(durationMillis = IOS_NAV_DURATION_MS, easing = IosNavEasing),
            targetOffsetX = { fullWidth -> fullWidth }
        )

        (enterTransition togetherWith exitTransition).apply {
            targetContentZIndex = -1f
        }
    }
}
