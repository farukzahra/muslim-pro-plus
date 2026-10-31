package com.farukzahra.muslimplus

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Ajuda de calibração da Qibla: abre, explica os passos e fecha. */
@RunWith(AndroidJUnit4::class)
class QiblaHelpTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun helpDialogExplainsCalibration() {
        runBlocking { ServiceLocator.settings.setOnboardingDone() }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        compose.onNodeWithText(context.getString(R.string.tab_qibla)).performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("qibla_help").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("qibla_help").performScrollTo().performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(context.getString(R.string.qibla_help_title))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.qibla_help_body)).assertExists()
        compose.onNodeWithText(context.getString(R.string.action_ok)).performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(context.getString(R.string.qibla_help_title))
                .fetchSemanticsNodes().isEmpty()
        }
    }
}
