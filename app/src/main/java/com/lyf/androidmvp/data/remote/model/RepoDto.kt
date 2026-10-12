package com.lyf.androidmvp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoSearchResponseDto(
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("items")
    val items: List<RepoDto> = emptyList()
)

@Serializable
data class RepoDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int = 0,
    @SerialName("forks_count")
    val forksCount: Int = 0,
    @SerialName("language")
    val language: String? = null,
    @SerialName("html_url")
    val htmlUrl: String = "",
    @SerialName("owner")
    val owner: RepoOwnerDto
)

@Serializable
data class RepoOwnerDto(
    @SerialName("login")
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)
