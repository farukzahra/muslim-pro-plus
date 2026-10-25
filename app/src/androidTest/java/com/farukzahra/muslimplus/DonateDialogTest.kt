package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.ui.screens.settings.PIX_KEY
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DonateDialogTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun moreCardOpensPixAndCloses() {
        runBlocking { ServiceLocator.settings.setOnboardingDone() }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        compose.onNodeWithText(context.getString(R.string.tab_more)).performClick()
        compose.onNodeWithTag("more_donate").performScrollTo().performClick()

        compose.onNodeWithTag("donate_dialog").assertIsDisplayed()
        compose.onNodeWithText(PIX_KEY).assertIsDisplayed()
        compose.onNodeWithTag("pix_copy").performClick()
        compose.onNodeWithText(context.getString(R.string.donate_copy)).performClick()
        compose.onNodeWithTag("donate_dialog").assertIsDisplayed()

        compose.onNodeWithText(context.getString(R.string.action_close)).performClick()
        compose.onNodeWithTag("donate_dialog").assertDoesNotExist()
    }
}
