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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private fun createFakeRepo(id: Long = 1L, isFavorite: Boolean = false) = Repo(
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
        isFavorite = isFavorite
    )

    private class FakeRepoRepository(initialRepo: Repo?) : RepoRepository {
        val repoState = MutableStateFlow(initialRepo)
        var toggleCalledCount = 0

        override fun getRepos(): Flow<List<Repo>> = repoState.map { listOfNotNull(it) }

        override fun getRepoById(id: Long): Flow<Repo?> = repoState.asStateFlow()

        override fun getFavoriteRepos(): Flow<List<Repo>> =
            repoState.map { listOfNotNull(it).filter { repo -> repo.isFavorite } }

        override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
            toggleCalledCount++
            repoState.value = repoState.value?.copy(isFavorite = isFavorite)
        }

        override suspend fun refreshRepos(forceRefresh: Boolean): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun loadMoreRepos(page: Int): AppResult<Boolean> = AppResult.Success(false)
        override suspend fun saveRemark(id: Long, remark: String) {}
        override suspend fun clearCache() {}
    }

    @Test
    fun `when repo is not favorite, invoke should toggle to true and persist`() = runTest {
        val fakeRepo = createFakeRepo(id = 1L, isFavorite = false)
        val repository = FakeRepoRepository(fakeRepo)
        val useCase = ToggleFavoriteUseCase(repository)

        val result = useCase(1L)

        assertTrue(result is AppResult.Success)
        assertTrue((result as AppResult.Success).data)
        assertEquals(1, repository.toggleCalledCount)
        assertTrue(repository.repoState.value?.isFavorite == true)
    }

    @Test
    fun `when repo is already favorite, invoke should toggle to false and persist`() = runTest {
        val fakeRepo = createFakeRepo(id = 1L, isFavorite = true)
        val repository = FakeRepoRepository(fakeRepo)
        val useCase = ToggleFavoriteUseCase(repository)

        val result = useCase(1L)

        assertTrue(result is AppResult.Success)
        assertFalse((result as AppResult.Success).data)
        assertEquals(1, repository.toggleCalledCount)
        assertFalse(repository.repoState.value?.isFavorite == true)
    }

    @Test
    fun `when repo does not exist, invoke should return error and not call repository`() = runTest {
        val repository = FakeRepoRepository(null)
        val useCase = ToggleFavoriteUseCase(repository)

        val result = useCase(999L)

        assertTrue(result is AppResult.Error)
        assertEquals("项目不存在或已被移除", (result as AppResult.Error).exception.message)
        assertEquals(0, repository.toggleCalledCount)
    }
}
