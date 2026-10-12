package com.lyf.androidmvp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repos")
data class RepoEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val htmlUrl: String,
    val cachedAt: Long = System.currentTimeMillis()
)
