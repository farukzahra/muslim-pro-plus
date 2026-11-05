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
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranReaderAudioTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun readerShowsSelectedVoiceAndOpensAudioStorage() {
        runBlocking {
            ServiceLocator.settings.setOnboardingDone()
            ServiceLocator.settings.setQuranSetupDone()
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity
        val original = runBlocking { ServiceLocator.settings.quranReciterId.first() }
        val voice = QuranReciters.all[2]
        runBlocking { ServiceLocator.settings.setQuranReciterId(voice.id) }
        try {
            compose.onNodeWithText(context.getString(R.string.tab_quran)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("Al-Fatihah").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onAllNodesWithText("Al-Fatihah")[0].performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText(voice.reciter).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onAllNodesWithText(voice.reciter)[0].assertIsDisplayed()
            // A voz abre em um toque, direto do leitor, na mesma folha da tela de áudio.
            val other = QuranReciters.all.first { it.id != voice.id }
            compose.onNodeWithTag("reader_change_voice").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("reciter_${other.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("reciter_${other.id}").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { ServiceLocator.settings.quranReciterId.first() } == other.id
            }
            compose.onNodeWithContentDescription(context.getString(R.string.quran_display)).performClick()
            compose.onNodeWithText(context.getString(R.string.audio_storage_title))
                .performScrollTo()
                .performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("clear_audio_cache").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("clear_audio_cache").assertExists()
        } finally {
            runBlocking { ServiceLocator.settings.setQuranReciterId(original) }
        }
    }
}
