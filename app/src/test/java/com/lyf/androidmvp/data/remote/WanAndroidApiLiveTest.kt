package com.lyf.androidmvp.data.remote

import com.lyf.androidmvp.data.remote.model.WanAndroidResponse
import com.lyf.androidmvp.data.remote.model.WanArticlePageDto
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class WanAndroidApiLiveTest {

    @Test
    fun getArticleList_parsesRealResponseCorrectly() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val apiService = retrofit.create(WanAndroidApiService::class.java)

        kotlinx.coroutines.runBlocking {
            val response: WanAndroidResponse<WanArticlePageDto> = apiService.getArticleList(page = 0, pageSize = 10)
            assertEquals(0, response.errorCode)
            assertNotNull(response.data)
            val articles = response.data!!.datas
            assertEquals("指定 pageSize=10 时必须精准返回 10 条真实文章", 10, articles.size)
            val firstArticle = articles.first()
            assertTrue("文章 ID 必须大于 0", firstArticle.id > 0)
            assertTrue("文章标题不能为空", firstArticle.title.isNotBlank())
            println("✅ 成功接入真实 WanAndroid 首页 API，首页精准返回 10 条，首条：【${firstArticle.title}】，作者：【${firstArticle.author.ifBlank { firstArticle.shareUser }}】")
        }
    }
}
