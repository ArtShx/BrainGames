package com.example.braingames

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class GameNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun selecting_memory_opens_game_screen() {
        composeRule.onNodeWithTag("play_Memory").performClick()
        composeRule.onNodeWithText("Memory").assertIsDisplayed()
        composeRule.onNodeWithText("Round 1/10").assertIsDisplayed()
        composeRule.onNodeWithText("Hearts: 3").assertIsDisplayed()
    }
}
