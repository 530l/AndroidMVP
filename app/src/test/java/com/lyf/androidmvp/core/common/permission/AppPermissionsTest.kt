package com.lyf.androidmvp.core.common.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppPermissionsTest {

    @Test
    fun `verify AppPermissions convenience getters return valid instances`() {
        assertNotNull(AppPermissions.Notification)
        assertNotNull(AppPermissions.Camera)
        assertNotNull(AppPermissions.Notification.permissionName)
        assertNotNull(AppPermissions.Camera.permissionName)
    }

    @Test
    fun `verify AppPermissionResult data contracts`() {
        val granted = AppPermissionResult.Granted(listOf(AppPermissions.Notification), allGranted = true)
        assertEquals(1, granted.permissions.size)
        assertEquals(true, granted.allGranted)

        val denied = AppPermissionResult.Denied(listOf(AppPermissions.Camera), doNotAskAgain = true)
        assertEquals(1, denied.permissions.size)
        assertEquals(true, denied.doNotAskAgain)
    }
}
