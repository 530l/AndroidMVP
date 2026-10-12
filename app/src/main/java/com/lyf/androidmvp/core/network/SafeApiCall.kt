package com.lyf.androidmvp.core.network

import com.lyf.androidmvp.core.common.AppException
import com.lyf.androidmvp.core.common.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): AppResult<T> = withContext(dispatcher) {
    try {
        val result = apiCall()
        AppResult.Success(result)
    } catch (e: CancellationException) {
        // 协程取消交由调用者处理，不能转换成普通业务错误。
        throw e
    } catch (e: HttpException) {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string()
        val exception = when (code) {
            401, 403 -> AppException.Unauthorized(
                message = "登录已失效或无权访问 ($code)",
                cause = e
            )
            in 500..599 -> AppException.Server(
                httpCode = code,
                message = "服务器内部错误 ($code)",
                cause = e
            )
            else -> AppException.Server(
                httpCode = code,
                message = errorBody?.takeIf { it.isNotBlank() } ?: "请求错误 ($code)",
                cause = e
            )
        }
        AppResult.Error(exception)
    } catch (e: SocketTimeoutException) {
        AppResult.Error(AppException.Network(message = "网络请求超时，请重试", cause = e))
    } catch (e: UnknownHostException) {
        AppResult.Error(AppException.Network(message = "无法解析服务器地址，请检查网络", cause = e))
    } catch (e: IOException) {
        AppResult.Error(AppException.Network(message = "网络连接异常: ${e.message}", cause = e))
    } catch (e: SerializationException) {
        AppResult.Error(AppException.Serialization(message = "数据解析失败: ${e.message}", cause = e))
    } catch (e: Exception) {
        AppResult.Error(AppException.Unknown(message = e.localizedMessage ?: "未知异常", cause = e))
    }
}
