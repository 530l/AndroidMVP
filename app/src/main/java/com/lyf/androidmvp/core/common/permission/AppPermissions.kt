package com.lyf.androidmvp.core.common.permission

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 常用权限便捷索引
 */
object AppPermissions {
    val Notification: IPermission get() = PermissionLists.getPostNotificationsPermission()
    val Camera: IPermission get() = PermissionLists.getCameraPermission()
}

/**
 * 权限请求结果封装
 */
sealed interface AppPermissionResult {
    /** 授予通过 */
    data class Granted(
        val permissions: List<IPermission>,
        val allGranted: Boolean
    ) : AppPermissionResult

    /** 被拒绝（包含是否被勾选“不再询问 / 永久拒绝”） */
    data class Denied(
        val permissions: List<IPermission>,
        val doNotAskAgain: Boolean
    ) : AppPermissionResult
}

/**
 * XXPermissions 挂起协程扩展：在协程中以同步线性代码形式发起权限请求
 *
 * 示例：
 * ```kotlin
 * val result = context.requestAppPermissions(AppPermissions.Notification, AppPermissions.Camera)
 * when (result) {
 *     is AppPermissionResult.Granted -> showMessage("授权成功")
 *     is AppPermissionResult.Denied -> {
 *         if (result.doNotAskAgain) context.openPermissionSettings(AppPermissions.Notification)
 *     }
 * }
 * ```
 */
suspend fun Context.requestAppPermissions(
    vararg permissions: IPermission
): AppPermissionResult = suspendCancellableCoroutine { continuation ->
    if (permissions.isEmpty()) {
        continuation.resume(AppPermissionResult.Granted(emptyList(), true))
        return@suspendCancellableCoroutine
    }

    XXPermissions.with(this)
        .permissions(permissions.toList())
        .unchecked()
        .request(object : OnPermissionCallback {
            override fun onPermissionResult(
                grantedPermissions: MutableList<IPermission>,
                deniedPermissions: MutableList<IPermission>
            ) {
                if (!continuation.isActive) return
                if (deniedPermissions.isEmpty()) {
                    continuation.resume(AppPermissionResult.Granted(grantedPermissions, true))
                } else {
                    val activity = this@requestAppPermissions as? Activity
                    val doNotAskAgain = activity?.let {
                        deniedPermissions.any { perm -> perm.isDoNotAskAgainPermission(it) }
                    } ?: false
                    continuation.resume(AppPermissionResult.Denied(deniedPermissions, doNotAskAgain))
                }
            }
        })
}

/**
 * 检查单个权限是否已被授予
 */
fun Context.isPermissionGranted(permission: IPermission): Boolean {
    return permission.isGrantedPermission(this)
}

/**
 * 检查一组权限是否全部被授予
 */
fun Context.isAllPermissionsGranted(vararg permissions: IPermission): Boolean {
    return permissions.all { it.isGrantedPermission(this) }
}

/**
 * 跳转到当前权限对应的应用系统设置页（支持特定权限精准跳转）
 */
fun Context.openPermissionSettings(vararg permissions: IPermission) {
    if (permissions.isEmpty()) {
        XXPermissions.startPermissionActivity(this)
    } else {
        XXPermissions.startPermissionActivity(this, *permissions)
    }
}

/**
 * Compose 专用的权限请求控制器
 */
class AppPermissionLauncher internal constructor(
    private val context: Context,
    private val onGranted: (List<IPermission>, Boolean) -> Unit,
    private val onDenied: (List<IPermission>, Boolean) -> Unit
) {
    fun launch(vararg permissions: IPermission) {
        if (permissions.isEmpty()) {
            onGranted(emptyList(), true)
            return
        }
        XXPermissions.with(context)
            .permissions(permissions.toList())
            .unchecked()
            .request { grantedPermissions, deniedPermissions ->
                if (deniedPermissions.isEmpty()) {
                    onGranted(grantedPermissions, true)
                } else {
                    val activity = context as? Activity
                    val doNotAskAgain = activity?.let {
                        deniedPermissions.any { perm -> perm.isDoNotAskAgainPermission(it) }
                    } ?: false
                    onDenied(deniedPermissions, doNotAskAgain)
                }
            }
    }
}

/**
 * Compose 声明式权限发射器 Hook
 */
@Composable
fun rememberAppPermissionLauncher(
    onGranted: (permissions: List<IPermission>, allGranted: Boolean) -> Unit = { _, _ -> },
    onDenied: (permissions: List<IPermission>, doNotAskAgain: Boolean) -> Unit = { _, _ -> }
): AppPermissionLauncher {
    val context = LocalContext.current
    val currentOnGranted = rememberUpdatedState(onGranted)
    val currentOnDenied = rememberUpdatedState(onDenied)

    return remember(context) {
        AppPermissionLauncher(
            context = context,
            onGranted = { perms, all -> currentOnGranted.value(perms, all) },
            onDenied = { perms, never -> currentOnDenied.value(perms, never) }
        )
    }
}
