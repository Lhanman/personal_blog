package com.personalblog.app.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenTest {
    @Test
    fun `home is top level`() {
        val chrome = resolveScreenChrome(Screen.Home.route)

        assertTrue(chrome.isTopLevel)
        assertEquals(null, chrome.fallbackRoute)
    }

    @Test
    fun `post detail falls back to posts`() {
        val chrome = resolveScreenChrome(Screen.PostDetail.route)

        assertFalse(chrome.isTopLevel)
        assertEquals(Screen.Posts.route, chrome.fallbackRoute)
    }

    @Test
    fun `admin edit falls back to admin posts`() {
        val chrome = resolveScreenChrome(Screen.AdminPostEdit.route)

        assertFalse(chrome.isTopLevel)
        assertEquals(Screen.AdminPostList.route, chrome.fallbackRoute)
    }

    @Test
    fun `admin users remains top level`() {
        val chrome = resolveScreenChrome(Screen.AdminUserList.route)

        assertTrue(chrome.isTopLevel)
        assertEquals(null, chrome.parentRoute)
    }
}
