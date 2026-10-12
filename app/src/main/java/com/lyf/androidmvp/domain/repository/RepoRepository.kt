package com.lyf.androidmvp.domain.repository

import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.domain.model.Repo
import kotlinx.coroutines.flow.Flow

interface RepoRepository {
    fun getRepos(): Flow<List<Repo>>
    suspend fun refreshRepos(forceRefresh: Boolean = true): AppResult<Unit>
    suspend fun loadMoreRepos(page: Int): AppResult<Boolean>
    fun getRepoById(id: Long): Flow<Repo?>
    fun getFavoriteRepos(): Flow<List<Repo>>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun saveRemark(id: Long, remark: String)
    suspend fun clearCache()
}
