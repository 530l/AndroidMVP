package com.lyf.androidmvp.data.mapper

import com.lyf.androidmvp.data.remote.model.WanArticleDto
import org.junit.Assert.assertEquals
import org.junit.Test

class WanArticleMapperTest {

    @Test
    fun toEntity_cleansHtmlEntities_andMapsFieldsCorrectly() {
        val dto = WanArticleDto(
            id = 31816L,
            title = "Kotlin 2026：裁员、AI、Rust&mdash;&mdash;黄金时代结束了吗？Jake Wharton 为你解答",
            author = "Jake Wharton",
            shareUser = "",
            chapterName = "Kotlin",
            superChapterName = "官方推荐",
            link = "https://www.wanandroid.com/blog/show/3932",
            niceDate = "2026-09-14 09:14",
            envelopePic = "",
            desc = "深度解析 Kotlin 演进",
            collect = false,
            zan = 1024
        )

        val entity = dto.toEntity()

        assertEquals(31816L, entity.id)
        assertEquals("Kotlin 2026：裁员、AI、Rust——黄金时代结束了吗？Jake Wharton 为你解答", entity.name)
        assertEquals("Jake Wharton", entity.ownerName)
        assertEquals(1024, entity.stars)
        assertEquals("官方推荐", entity.language)
        assertEquals("深度解析 Kotlin 演进", entity.description)
        assertEquals("https://www.wanandroid.com/blog/show/3932", entity.htmlUrl)
    }

    @Test
    fun toEntity_handlesEmptyAuthorAndEnvelopePic() {
        val dto = WanArticleDto(
            id = 100L,
            title = "Compose 最佳实践",
            author = "",
            shareUser = "panoogunker",
            chapterName = "Compose",
            superChapterName = "UI体系",
            link = "https://juejin.cn/post/123",
            niceDate = "2026-10-10",
            envelopePic = "",
            desc = "",
            zan = 5
        )

        val entity = dto.toEntity()

        assertEquals("panoogunker", entity.ownerName)
        assertEquals("分类：UI体系 · Compose | 时间：2026-10-10", entity.description)
        assertEquals("https://www.wanandroid.com/resources/image/pc/default_project_img.png", entity.ownerAvatarUrl)
    }
}
