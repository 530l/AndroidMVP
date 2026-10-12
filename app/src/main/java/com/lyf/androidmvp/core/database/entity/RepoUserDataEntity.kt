package com.lyf.androidmvp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 用户数据不依赖缓存记录的生命周期，清理缓存时仍然保留。
@Entity(tableName = "repo_user_data")
data class RepoUserDataEntity(
    @PrimaryKey val repoId: Long,
    val isFavorite: Boolean = false,
    val userRemark: String = ""
)
