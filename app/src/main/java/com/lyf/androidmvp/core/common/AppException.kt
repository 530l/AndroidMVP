package com.lyf.androidmvp.core.common

sealed class AppException(
    val code: Int = -1,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class Network(
        override val message: String = "网络连接异常，请检查网络设置",
        override val cause: Throwable? = null
    ) : AppException(code = 1001, message = message, cause = cause)

    data class Server(
        val httpCode: Int,
        override val message: String = "服务器开小差了，请稍后重试",
        override val cause: Throwable? = null
    ) : AppException(code = httpCode, message = message, cause = cause)

    data class Unauthorized(
        override val message: String = "登录凭证已过期，请重新登录",
        override val cause: Throwable? = null
    ) : AppException(code = 401, message = message, cause = cause)

    data class Serialization(
        override val message: String = "数据解析异常",
        override val cause: Throwable? = null
    ) : AppException(code = 1002, message = message, cause = cause)

    data class Business(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppException(code = 2001, message = message, cause = cause)

    data class Unknown(
        override val message: String = "未知错误发生",
        override val cause: Throwable? = null
    ) : AppException(code = 9999, message = message, cause = cause)
}
