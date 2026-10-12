package com.lyf.androidmvp.domain.usecase

import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.domain.model.Repo
import com.lyf.androidmvp.domain.repository.RepoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveRepoRemarkUseCaseTest {

    private fun createFakeRepo(id: Long = 1L, userRemark: String = "") = Repo(
        id = id,
        name = "AndroidMVP",
        fullName = "lyf/AndroidMVP",
        description = "Commercial Android Scaffold",
        ownerName = "lyf",
        ownerAvatarUrl = "https://example.com/avatar.png",
        stars = 9999,
        forks = 888,
        language = "Kotlin",
        htmlUrl = "https://github.com/lyf/AndroidMVP",
        userRemark = userRemark
    )

    private class FakeRepoRepository(initialRepo: Repo?) : RepoRepository {
        val repoState = MutableStateFlow(initialRepo)
        var saveRemarkCalledCount = 0
        var lastSavedRemark: String? = null

        override fun getRepos(): Flow<List<Repo>> = repoState.map { listOfNotNull(it) }

        override fun getRepoById(id: Long): Flow<Repo?> = repoState.asStateFlow()

        override fun getFavoriteRepos(): Flow<List<Repo>> = flowOf(emptyList())

        override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {}

        override suspend fun refreshRepos(forceRefresh: Boolean): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun loadMoreRepos(page: Int): AppResult<Boolean> = AppResult.Success(false)

        override suspend fun saveRemark(id: Long, remark: String) {
            saveRemarkCalledCount++
            lastSavedRemark = remark
            repoState.value = repoState.value?.copy(userRemark = remark)
        }

        override suspend fun clearCache() {}
    }

    @Test
    fun `when remark is valid, invoke should trim whitespace and persist to repository`() = runTest {
        val fakeRepo = createFakeRepo(id = 1L)
        val repository = FakeRepoRepository(fakeRepo)
        val useCase = SaveRepoRemarkUseCase(repository)

        val result = useCase(1L, "  这是一个重要项目  ")

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.saveRemarkCalledCount)
        assertEquals("这是一个重要项目", repository.lastSavedRemark)
    }

    @Test
    fun `when remark exceeds 200 chars, invoke should return error and not call repository`() = runTest {
        val fakeRepo = createFakeRepo(id = 1L)
        val repository = FakeRepoRepository(fakeRepo)
        val useCase = SaveRepoRemarkUseCase(repository)

        val overlyLongRemark = "A".repeat(201)
        val result = useCase(1L, overlyLongRemark)

        assertTrue(result is AppResult.Error)
        assertEquals("备注长度不能超过 200 个字符", (result as AppResult.Error).exception.message)
        assertEquals(0, repository.saveRemarkCalledCount)
    }

    @Test
    fun `when repo does not exist, invoke should return error and not call repository`() = runTest {
        val repository = FakeRepoRepository(null)
        val useCase = SaveRepoRemarkUseCase(repository)

        val result = useCase(999L, "备注")

        assertTrue(result is AppResult.Error)
        assertEquals("项目不存在或已被移除", (result as AppResult.Error).exception.message)
        assertEquals(0, repository.saveRemarkCalledCount)
    }

    @Test
    fun `when remark is identical to existing remark, dirty check should skip repository write`() = runTest {
        val fakeRepo = createFakeRepo(id = 1L, userRemark = "已有备注")
        val repository = FakeRepoRepository(fakeRepo)
        val useCase = SaveRepoRemarkUseCase(repository)

        val result = useCase(1L, "已有备注")

        assertTrue(result is AppResult.Success)
        assertEquals(0, repository.saveRemarkCalledCount) // 脏检查生效，零 I/O 开销
    }
}
