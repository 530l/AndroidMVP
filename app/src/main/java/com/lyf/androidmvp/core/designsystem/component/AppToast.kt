package com.lyf.androidmvp.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.hjq.toast.Toaster
import com.lyf.androidmvp.core.common.UiMessage

/**
 * 全局 AppToast 调度中心与工具类（基于开源库 getActivity/Toaster 封装）
 *
 * 【为什么选用 Toaster 库？】
 * 1. 疑难杂症治理：专治 Android 7.1.1 的 BadTokenException、Android 11+ 后台无法弹 Toast 限制以及通知权限被关后的吐司失效。
 * 2. 避免队列积压：传统 Toast 连续弹出时会排队数秒延迟，Toaster 自动处理当前最新消息覆盖，交互即时轻快。
 * 3. 优雅样式与跨平台稳定性：开箱即用圆角、阴影与动画，且与 Jetpack Compose 单向数据流（UDF）无缝协同。
 */
object AppToast {

    /**
     * 显示短 Toast 提示
     */
    fun show(text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            Toaster.show(text)
        }
    }

    /**
     * 根据字符串资源 ID 显示 Toast
     */
    fun show(id: Int) {
        Toaster.show(id)
    }

    /**
     * 显示长 Toast 提示
     */
    fun showLong(text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            Toaster.showLong(text)
        }
    }

    /**
     * 取消当前展示中的 Toast
     */
    fun cancel() {
        Toaster.cancel()
    }

    /**
     * 仅在 Debug 模式下输出 Toast
     */
    fun debugShow(text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            Toaster.debugShow(text)
        }
    }
}

/**
 * 监听 [UiMessage] 列表并通过 [Toaster] 弹出 Toast 提示。
 *
 * 保证单向数据流与单次安全消费：
 * 1. 当 ViewModel 产生新的 [UiMessage] 时，即刻触发 [AppToast.show]；
 * 2. 随即触发 [onMessageShown]，将已消费的 Message 从 UiState 中剔除，杜绝切屏或重组时的消息倒灌重复播放。
 */
@Composable
fun AppToastMessages(
    messages: List<UiMessage>,
    onMessageShown: (String) -> Unit
) {
    val onShown by rememberUpdatedState(onMessageShown)
    val handledIds = remember { mutableSetOf<String>() }
    val message = messages.firstOrNull()

    LaunchedEffect(message?.id) {
        if (message != null && message.id !in handledIds) {
            handledIds.add(message.id)
            if (handledIds.size > 50) {
                handledIds.clear()
                handledIds.add(message.id)
            }
            AppToast.show(message.text)
            onShown(message.id)
        }
    }
}
