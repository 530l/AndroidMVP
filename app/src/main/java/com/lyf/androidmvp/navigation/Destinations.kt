package com.lyf.androidmvp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppDestination : NavKey {

    @Serializable
    data object Main : AppDestination

    /**
     * 详情页路由 - 支持多参数强类型传参
     * @param repoId 项目唯一 ID
     * @param repoName 项目名称
     * @param fromSource 来源入口标识 (例如 "首页"、"搜索"、"推荐")
     */
    @Serializable
    data class Detail(
        val repoId: Long,
        val repoName: String,
        val fromSource: String = "首页"
    ) : AppDestination

    @Serializable
    data object Settings : AppDestination
}
