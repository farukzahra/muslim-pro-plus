package com.farukzahra.muslimplus

import androidx.compose.ui.semantics.SemanticsProperties
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Card do verso do dia: rolagem, outro verso, esconder e restaurar. */
@RunWith(AndroidJUnit4::class)
class TodayVerseTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun cardScrollsRefreshesAndCanBeHidden() {
        val settings = ServiceLocator.settings
        val originalModes = runBlocking { settings.quranModes.first() }
        val originalVisible = runBlocking { settings.dailyVerseVisible.first() }
        runBlocking {
            settings.setOnboardingDone()
            settings.setQuranModes(setOf("ar", "translit", "pt-BR"))
            settings.setDailyVerseVisible(true)
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        try {
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("daily_verse_card").fetchSemanticsNodes().isNotEmpty()
            }

            val firstReference = referenceText()
            compose.onNodeWithTag("daily_verse_refresh").performClick()
            compose.waitUntil(5_000) { referenceText() != firstReference }

            compose.onNodeWithText(context.getString(R.string.prayer_isha))
                .performScrollTo()
                .assertIsDisplayed()

            compose.onNodeWithTag("daily_verse_hide").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("daily_verse_card").fetchSemanticsNodes().isEmpty()
            }
            compose.onNodeWithTag("daily_verse_card").assertDoesNotExist()

            runBlocking { settings.setDailyVerseVisible(true) }
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("daily_verse_card").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("daily_verse_card").assertIsDisplayed()
        } finally {
            runBlocking {
                settings.setQuranModes(originalModes)
                settings.setDailyVerseVisible(originalVisible)
            }
        }
    }

    private fun referenceText(): String =
        compose.onNodeWithTag("daily_verse_reference")
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString("") { it.text }
}
