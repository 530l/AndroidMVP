package com.lyf.androidmvp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun `Success result returns correct data and executes onSuccess`() {
        var callbackExecuted = false
        val result: AppResult<String> = AppResult.Success("Android Enterprise")

        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals("Android Enterprise", result.getOrNull())

        result.onSuccess {
            callbackExecuted = true
            assertEquals("Android Enterprise", it)
        }
        assertTrue(callbackExecuted)
    }

    @Test
    fun `Error result preserves exception and executes onError`() {
        var callbackExecuted = false
        val exception = AppException.Network(message = "Network unreachable")
        val result: AppResult<String> = AppResult.Error(exception)

        assertTrue(result.isError)
        assertFalse(result.isSuccess)
        assertEquals(null, result.getOrNull())

        result.onError {
            callbackExecuted = true
            assertEquals(1001, it.code)
            assertEquals("Network unreachable", it.message)
        }
        assertTrue(callbackExecuted)
    }

    @Test
    fun `map transforms Success value properly`() {
        val result: AppResult<Int> = AppResult.Success(100)
        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess)
        assertEquals(200, (mapped as AppResult.Success).data)
    }
}
