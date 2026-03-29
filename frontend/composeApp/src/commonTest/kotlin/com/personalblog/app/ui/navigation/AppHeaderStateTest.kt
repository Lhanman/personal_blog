package com.personalblog.app.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppHeaderStateTest {
    @Test
    fun `nested screen shows back button when history exists`() {
        val state = AppHeaderState(
            currentRoute = Screen.PostDetail.route,
            screenChrome = resolveScreenChrome(Screen.PostDetail.route),
            canNavigateBack = true
        )

        assertTrue(state.showBackButton)
        assertEquals(Screen.Posts.route, state.fallbackRoute)
    }

    @Test
    fun `top level screen hides back button`() {
        val state = AppHeaderState(
            currentRoute = Screen.Home.route,
            screenChrome = resolveScreenChrome(Screen.Home.route),
            canNavigateBack = false
        )

        assertFalse(state.showBackButton)
        assertEquals(null, state.fallbackRoute)
    }
}
