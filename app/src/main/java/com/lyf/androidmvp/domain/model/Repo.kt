package com.lyf.androidmvp.domain.model

data class Repo(
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
    val isFavorite: Boolean = false,
    val userRemark: String = ""
)
