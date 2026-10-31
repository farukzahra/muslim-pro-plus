package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Wizard de primeira entrada no Alcorão: três passos, escolhas salvas e início. */
@RunWith(AndroidJUnit4::class)
class QuranSetupTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val settings = ServiceLocator.settings

    @Test
    fun wizardSavesTranslationAndGoalThenOpensTheList() {
        val originalModes = runBlocking { settings.quranModes.first() }
        val originalGoal = runBlocking { settings.quranGoalMinutes.first() }
        runBlocking {
            settings.setOnboardingDone()
            settings.setQuranSetupDone(false)
            settings.setQuranModes(setOf("ar", "en"))
            settings.setQuranGoalMinutes(10)
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        try {
            compose.onNodeWithText(context.getString(R.string.tab_quran)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("quran_setup").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(
                context.getString(R.string.quran_setup_step, 1, 3),
            ).assertIsDisplayed()

            compose.onNodeWithTag("quran_setup_next").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("quran_setup_translation_pt")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("quran_setup_translation_pt").performClick()
            compose.waitUntil(5_000) {
                runBlocking { settings.quranModes.first() == setOf("ar", "pt-BR") }
            }

            compose.onNodeWithTag("quran_setup_next").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("quran_setup_goal_15").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("quran_setup_goal_15").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { settings.quranGoalMinutes.first() == 15 }
            }
            compose.onNodeWithTag("quran_setup_khatam").assertIsDisplayed()

            compose.onNodeWithTag("quran_setup_start").performScrollTo().performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("quran_setup").fetchSemanticsNodes().isEmpty()
            }
            assertEquals(true, runBlocking { settings.quranSetupDone.first() })
            compose.onNodeWithText("Al-Fatihah").assertExists()
        } finally {
            runBlocking {
                settings.setQuranSetupDone()
                settings.setQuranModes(originalModes)
                settings.setQuranGoalMinutes(originalGoal)
            }
        }
    }
}
