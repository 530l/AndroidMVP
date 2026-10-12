package com.lyf.androidmvp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiMessageTest {

    @Test
    fun `test UiMessage creation has unique ids and correct text`() {
        val msg1 = UiMessage("收藏成功")
        val msg2 = UiMessage("收藏成功")

        assertEquals("收藏成功", msg1.text)
        assertEquals("收藏成功", msg2.text)
        assertNotEquals(msg1.id, msg2.id)
    }

    @Test
    fun `test message dismissal removes targeted message by id`() {
        val msg1 = UiMessage("收藏成功")
        val msg2 = UiMessage("操作失败")
        val messages = listOf(msg1, msg2)

        val updated = messages.filterNot { it.id == msg1.id }
        assertEquals(1, updated.size)
        assertEquals(msg2.id, updated.first().id)

        val empty = updated.filterNot { it.id == msg2.id }
        assertTrue(empty.isEmpty())
    }
}
