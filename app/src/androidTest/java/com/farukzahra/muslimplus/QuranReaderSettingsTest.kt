package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.settings.ReaderFont
import com.farukzahra.muslimplus.settings.ReaderMode
import com.farukzahra.muslimplus.settings.ReaderTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Bottom sheet do leitor: tema, fonte, modo, tela ligada e modo Página. */
@RunWith(AndroidJUnit4::class)
class QuranReaderSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val settings = ServiceLocator.settings

    @Test
    fun readerPrefsAreSavedAndPageModeShowsOneVerse() {
        val originalTheme = runBlocking { settings.readerTheme.first() }
        val originalFont = runBlocking { settings.readerFont.first() }
        val originalMode = runBlocking { settings.readerMode.first() }
        val originalKeep = runBlocking { settings.readerKeepScreenOn.first() }
        runBlocking {
            settings.setOnboardingDone()
            settings.setQuranSetupDone()
            settings.setReaderTheme(ReaderTheme.MODERN)
            settings.setReaderFont(ReaderFont.MEDIUM)
            settings.setReaderMode(ReaderMode.LIST)
            settings.setReaderKeepScreenOn(false)
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        try {
            compose.onNodeWithText(context.getString(R.string.tab_quran)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("Al-Fatihah").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onAllNodesWithText("Al-Fatihah")[0].performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText(
                    requireNotNull(ServiceLocator.quran.verse(1, 1)).text,
                ).fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithContentDescription(context.getString(R.string.quran_display)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("reader_sheet").fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithTag("reader_theme_paper").performClick()
            compose.waitUntil(5_000) {
                runBlocking { settings.readerTheme.first() == ReaderTheme.PAPER }
            }

            compose.onNodeWithTag("reader_font_large").performClick()
            compose.waitUntil(5_000) {
                runBlocking { settings.readerFont.first() == ReaderFont.LARGE }
            }

            compose.onNodeWithTag("reader_mode_page").performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("reader_pager").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("reader_page_0").assertExists()

            compose.onNodeWithTag("reader_keep_screen").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { settings.readerKeepScreenOn.first() }
            }
            assertTrue(runBlocking { settings.readerKeepScreenOn.first() })

            compose.onNodeWithTag("reader_sheet_close").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("reader_sheet").fetchSemanticsNodes().isEmpty()
            }
            assertEquals(ReaderMode.PAGE, runBlocking { settings.readerMode.first() })
        } finally {
            runBlocking {
                settings.setReaderTheme(originalTheme)
                settings.setReaderFont(originalFont)
                settings.setReaderMode(originalMode)
                settings.setReaderKeepScreenOn(originalKeep)
            }
        }
    }
}
