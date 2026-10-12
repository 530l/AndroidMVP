package com.lyf.androidmvp.data.remote

import com.lyf.androidmvp.data.remote.model.WanAndroidResponse
import com.lyf.androidmvp.data.remote.model.WanArticlePageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WanAndroidApiService {

    /**
     * 获取 WanAndroid 首页文章列表
     * @param page 页码，从 0 开始
     * @param pageSize 每页条数，默认 10 条
     */
    @GET("article/list/{page}/json")
    suspend fun getArticleList(
        @Path("page") page: Int = 0,
        @Query("page_size") pageSize: Int = 10
    ): WanAndroidResponse<WanArticlePageDto>
}
