package com.lyf.androidmvp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WanAndroidResponse<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("errorCode")
    val errorCode: Int = 0,
    @SerialName("errorMsg")
    val errorMsg: String = ""
)

@Serializable
data class WanArticlePageDto(
    @SerialName("curPage")
    val curPage: Int = 0,
    @SerialName("datas")
    val datas: List<WanArticleDto> = emptyList(),
    @SerialName("offset")
    val offset: Int = 0,
    @SerialName("over")
    val over: Boolean = false,
    @SerialName("pageCount")
    val pageCount: Int = 0,
    @SerialName("size")
    val size: Int = 0,
    @SerialName("total")
    val total: Int = 0
)

@Serializable
data class WanArticleDto(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String = "",
    @SerialName("author")
    val author: String = "",
    @SerialName("shareUser")
    val shareUser: String = "",
    @SerialName("chapterName")
    val chapterName: String = "",
    @SerialName("superChapterName")
    val superChapterName: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("niceDate")
    val niceDate: String = "",
    @SerialName("envelopePic")
    val envelopePic: String = "",
    @SerialName("desc")
    val desc: String = "",
    @SerialName("collect")
    val collect: Boolean = false,
    @SerialName("zan")
    val zan: Int = 0
)
