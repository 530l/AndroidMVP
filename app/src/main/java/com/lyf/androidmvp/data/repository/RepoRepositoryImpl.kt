package com.lyf.androidmvp.data.repository

import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.core.common.IoDispatcher
import com.lyf.androidmvp.core.database.dao.RepoDao
import com.lyf.androidmvp.core.network.safeApiCall
import com.lyf.androidmvp.data.mapper.toDomain
import com.lyf.androidmvp.data.remote.WanAndroidApiService
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.domain.repository.RepoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 仓库数据源实现类 (当前切换为纯内存数据源，一页 10 条数据，解耦数据库)
 */
@Singleton
class RepoRepositoryImpl @Inject constructor(
    private val wanAndroidApiService: WanAndroidApiService,
    private val repoDao: RepoDao, // 保留注入，后续切回 Room 数据库时无需修改 Hilt 依赖拓扑图
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RepoRepository {

    private val cacheMutex = Mutex()
    // 内存数据流 (不使用数据库，作为当前 UI 的唯一可信数据源)
    private val _reposFlow = MutableStateFlow<List<Repo>>(emptyList())
    // 内存维护收藏与备注状态，保证离开页面或筛选时不丢失操作
    private val userFavorites = mutableMapOf<Long, Boolean>()
    private val userRemarks = mutableMapOf<Long, String>()

    override fun getRepos(): Flow<List<Repo>> = _reposFlow.asStateFlow()

    override suspend fun refreshRepos(forceRefresh: Boolean): AppResult<Unit> =
        safeApiCall(ioDispatcher) {
            cacheMutex.withLock {
                // 请求真实 WanAndroid 首页文章列表第 0 页，指定每页 10 条数据
                val response = wanAndroidApiService.getArticleList(page = 0, pageSize = PAGE_SIZE)
                val articles = response.data?.datas.orEmpty()
                val domainList = articles.map { dto ->
                    dto.toDomain(
                        isFavorite = userFavorites[dto.id] ?: dto.collect,
                        userRemark = userRemarks[dto.id].orEmpty()
                    )
                }
                // 纯内存重置为第一页 10 条数据
                _reposFlow.value = domainList
            }
        }

    override suspend fun loadMoreRepos(page: Int): AppResult<Boolean> =
        safeApiCall(ioDispatcher) {
            cacheMutex.withLock {
                // 请求真实 WanAndroid 下一页数据，指定每页 10 条数据 (page 从 1 开始累加)
                val response = wanAndroidApiService.getArticleList(page = page, pageSize = PAGE_SIZE)
                val pageData = response.data
                val articles = pageData?.datas.orEmpty()
                if (articles.isNotEmpty()) {
                    val newDomainList = articles.map { dto ->
                        dto.toDomain(
                            isFavorite = userFavorites[dto.id] ?: dto.collect,
                            userRemark = userRemarks[dto.id].orEmpty()
                        )
                    }
                    // 内存追加并按 ID 去重
                    val existingIds = _reposFlow.value.map { it.id }.toSet()
                    val distinctNewItems = newDomainList.filter { it.id !in existingIds }
                    _reposFlow.value = _reposFlow.value + distinctNewItems
                }
                val isOver = pageData?.over ?: (articles.size < PAGE_SIZE)
                !isOver
            }
        }

    override fun getRepoById(id: Long): Flow<Repo?> = _reposFlow
        .map { list -> list.find { it.id == id } }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    override fun getFavoriteRepos(): Flow<List<Repo>> = _reposFlow
        .map { list -> list.filter { it.isFavorite } }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(ioDispatcher) {
        cacheMutex.withLock {
            userFavorites[id] = isFavorite
            _reposFlow.update { list ->
                list.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
            }
        }
    }

    override suspend fun saveRemark(id: Long, remark: String) = withContext(ioDispatcher) {
        cacheMutex.withLock {
            userRemarks[id] = remark
            _reposFlow.update { list ->
                list.map { if (it.id == id) it.copy(userRemark = remark) else it }
            }
        }
    }

    override suspend fun clearCache() = withContext(ioDispatcher) {
        cacheMutex.withLock {
            _reposFlow.value = emptyList()
            userFavorites.clear()
            userRemarks.clear()
        }
    }

    companion object {
        const val PAGE_SIZE = 10 // 一页 10 条数据
    }
}
