package com.lyf.androidmvp.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.lyf.androidmvp.core.database.entity.RepoEntity
import com.lyf.androidmvp.core.database.entity.RepoWithUserData
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoDao {

    @Query("SELECT repos.*, COALESCE(user.isFavorite, 0) AS isFavorite, COALESCE(user.userRemark, '') AS userRemark FROM repos LEFT JOIN repo_user_data AS user ON repos.id = user.repoId ORDER BY repos.id DESC")
    fun getAllRepos(): Flow<List<RepoWithUserData>>

    @Query("SELECT repos.*, COALESCE(user.isFavorite, 0) AS isFavorite, COALESCE(user.userRemark, '') AS userRemark FROM repos LEFT JOIN repo_user_data AS user ON repos.id = user.repoId WHERE repos.id = :id")
    fun getRepoById(id: Long): Flow<RepoWithUserData?>

    @Query("SELECT repos.*, user.isFavorite, user.userRemark FROM repos INNER JOIN repo_user_data AS user ON repos.id = user.repoId WHERE user.isFavorite = 1 ORDER BY repos.id DESC")
    fun getFavoriteRepos(): Flow<List<RepoWithUserData>>

    @Query("SELECT EXISTS(SELECT 1 FROM repos)")
    suspend fun hasCachedRepos(): Boolean

    @Upsert
    suspend fun upsertRepos(repos: List<RepoEntity>)

    @Query("INSERT OR IGNORE INTO repo_user_data (repoId, isFavorite, userRemark) VALUES (:id, 0, '')")
    suspend fun ensureUserData(id: Long)

    @Query("UPDATE repo_user_data SET isFavorite = :isFavorite WHERE repoId = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Transaction
    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        ensureUserData(id)
        setFavorite(id, isFavorite)
    }

    @Query("UPDATE repo_user_data SET userRemark = :remark WHERE repoId = :id")
    suspend fun setRemark(id: Long, remark: String)

    @Transaction
    suspend fun updateRemark(id: Long, remark: String) {
        ensureUserData(id)
        setRemark(id, remark)
    }

    @Query("DELETE FROM repos")
    suspend fun clearRepos()
}
