package com.lyf.androidmvp.feature.detail

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 详情页回传给前序页面的业务结果模型 (回参)
 */
@Serializable
data class DetailResult(
    val repoId: Long,
    val isFavorite: Boolean,
    val userRemark: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val resultId: String = UUID.randomUUID().toString()
)
