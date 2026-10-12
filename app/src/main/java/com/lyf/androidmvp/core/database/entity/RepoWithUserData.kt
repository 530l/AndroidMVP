package com.lyf.androidmvp.core.database.entity

import androidx.room.Embedded

data class RepoWithUserData(
    @Embedded val repo: RepoEntity,
    val isFavorite: Boolean,
    val userRemark: String
)
