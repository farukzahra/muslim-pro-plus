package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdhanScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun defaultsPersistAndChoicesAreSaved() {
        val settings = ServiceLocator.settings
        val originalPrayers = runBlocking { settings.adhanPrayers.first() }
        val originalSound = runBlocking { settings.adhanSoundId.first() }
        val originalRespect = runBlocking { settings.adhanRespectSilent.first() }
        val originalReminder = runBlocking { settings.adhanReminderMinutes.first() }
        runBlocking {
            settings.setOnboardingDone()
            settings.setAdhanPrayers(AdhanPolicy.defaultEnabled)
            settings.setAdhanSound(AdhanSound.default.id)
            settings.setAdhanRespectSilent(false)
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        try {
            compose.onNodeWithText(context.getString(R.string.tab_more)).performClick()
            compose.onNodeWithText(context.getString(R.string.more_settings)).performClick()
            compose.onNodeWithText(context.getString(R.string.settings_adhan))
                .performScrollTo()
                .performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("adhan_fajr").fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithTag("adhan_fajr").assertIsOff()
            compose.onNodeWithTag("adhan_dhuhr").assertIsOn()

            compose.onNodeWithTag("adhan_fajr").performClick()
            compose.waitUntil(5_000) {
                runBlocking { Prayer.FAJR in ServiceLocator.settings.adhanPrayers.first() }
            }
            compose.onNodeWithTag("adhan_fajr").assertIsOn()

            compose.onNodeWithTag("adhan_sound_madinah-bukhari").performClick()
            compose.waitUntil(5_000) {
                runBlocking { ServiceLocator.settings.adhanSoundId.first() == "madinah-bukhari" }
            }

            compose.onNodeWithTag("adhan_respect_silent").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { ServiceLocator.settings.adhanRespectSilent.first() }
            }

            compose.onNodeWithTag("adhan_reminder_15").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { ServiceLocator.settings.adhanReminderMinutes.first() == 15 }
            }
            compose.onNodeWithTag("adhan_reminder_0").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { ServiceLocator.settings.adhanReminderMinutes.first() == 0 }
            }
        } finally {
            runBlocking {
                settings.setAdhanPrayers(originalPrayers)
                settings.setAdhanSound(originalSound)
                settings.setAdhanRespectSilent(originalRespect)
                settings.setAdhanReminderMinutes(originalReminder)
            }
        }
    }
}
