package com.lyf.androidmvp.core.network.interceptor

import com.lyf.androidmvp.BuildConfig
import com.lyf.androidmvp.core.datastore.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 核心优化：直接读取内存级 Token 镜像（0ms），彻底杜绝 runBlocking 阻塞 OkHttp 线程池
        val token = appPreferences.getCachedToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("X-Client-Platform", "Android")
            .header("X-App-Version", BuildConfig.VERSION_NAME)

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
