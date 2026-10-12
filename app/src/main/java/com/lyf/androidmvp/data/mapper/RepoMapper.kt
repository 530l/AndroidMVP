package com.lyf.androidmvp.data.mapper

import com.lyf.androidmvp.core.database.entity.RepoEntity
import com.lyf.androidmvp.core.database.entity.RepoWithUserData
import com.lyf.androidmvp.data.remote.model.RepoDto
import com.lyf.androidmvp.data.remote.model.WanArticleDto
import com.lyf.androidmvp.domain.model.Repo

fun WanArticleDto.toEntity(): RepoEntity {
    val cleanTitle = title.replace("&mdash;", "—")
        .replace("&ldquo;", "“")
        .replace("&rdquo;", "”")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace(Regex("<[^>]*>"), "")
        .trim()

    val authorDisplay = author.ifBlank { shareUser.ifBlank { "玩Android用户" } }
    val categoryDisplay = listOf(superChapterName, chapterName).filter { it.isNotBlank() }.joinToString(" · ")
    val finalDesc = desc.ifBlank {
        if (categoryDisplay.isNotBlank()) "分类：$categoryDisplay | 时间：$niceDate" else niceDate
    }

    return RepoEntity(
        id = id,
        name = cleanTitle,
        fullName = cleanTitle,
        description = finalDesc,
        ownerName = authorDisplay,
        ownerAvatarUrl = envelopePic.ifBlank {
            "https://www.wanandroid.com/resources/image/pc/default_project_img.png"
        },
        stars = zan,
        forks = 0,
        language = superChapterName.ifBlank { chapterName },
        htmlUrl = link
    )
}

fun WanArticleDto.toDomain(isFavorite: Boolean = false, userRemark: String = ""): Repo {
    val cleanTitle = title.replace("&mdash;", "—")
        .replace("&ldquo;", "“")
        .replace("&rdquo;", "”")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace(Regex("<[^>]*>"), "")
        .trim()

    val authorDisplay = author.ifBlank { shareUser.ifBlank { "玩Android用户" } }
    val categoryDisplay = listOf(superChapterName, chapterName).filter { it.isNotBlank() }.joinToString(" · ")
    val finalDesc = desc.ifBlank {
        if (categoryDisplay.isNotBlank()) "分类：$categoryDisplay | 时间：$niceDate" else niceDate
    }

    return Repo(
        id = id,
        name = cleanTitle,
        fullName = cleanTitle,
        description = finalDesc,
        ownerName = authorDisplay,
        ownerAvatarUrl = envelopePic.ifBlank {
            "https://www.wanandroid.com/resources/image/pc/default_project_img.png"
        },
        stars = zan,
        forks = 0,
        language = superChapterName.ifBlank { chapterName },
        htmlUrl = link,
        isFavorite = isFavorite,
        userRemark = userRemark
    )
}

fun RepoDto.toEntity(): RepoEntity {
    return RepoEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerName = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        stars = stargazersCount,
        forks = forksCount,
        language = language,
        htmlUrl = htmlUrl
    )
}

fun RepoWithUserData.toDomain(): Repo = repo.toDomain(isFavorite, userRemark)

private fun RepoEntity.toDomain(isFavorite: Boolean, userRemark: String): Repo {
    return Repo(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerName = ownerName,
        ownerAvatarUrl = ownerAvatarUrl,
        stars = stars,
        forks = forks,
        language = language,
        htmlUrl = htmlUrl,
        isFavorite = isFavorite,
        userRemark = userRemark
    )
}

fun List<RepoWithUserData>.toDomainList(): List<Repo> = map { it.toDomain() }
