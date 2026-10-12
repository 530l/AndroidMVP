package com.lyf.androidmvp.data.remote

import com.lyf.androidmvp.data.remote.model.RepoSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GithubApiService {

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String = "language:kotlin stars:>1000",
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 30
    ): RepoSearchResponseDto
}
