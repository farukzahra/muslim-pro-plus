package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Sheet por oração aberta pelo sino do Hoje. */
@RunWith(AndroidJUnit4::class)
class PrayerAdhanSheetTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val settings = ServiceLocator.settings

    private fun reset() {
        runBlocking {
            settings.setOnboardingDone()
            settings.setAdhanPrayers(AdhanPolicy.defaultEnabled)
            settings.setAdhanSoundsByPrayer(emptyMap())
            settings.setAdhanRemindersByPrayer(emptyMap())
            settings.setAdhanAlarmByPrayer(emptySet())
            settings.setAdhanRepeatsByPrayer(emptyMap())
        }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("prayer_bell_fajr").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun open(prayer: Prayer) {
        compose.onNodeWithTag("prayer_bell_${prayer.name.lowercase()}")
            .performScrollTo()
            .performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("prayer_adhan_notify").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun soundAlarmAndDaysAreSavedOnlyForThatPrayer() {
        reset()
        open(Prayer.FAJR)

        // Sem mudança, Salvar está desabilitado.
        compose.onNodeWithTag("prayer_adhan_save").assertIsNotEnabled()

        compose.onNodeWithTag("prayer_adhan_sound_row").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("prayer_sound_option_fajr-makkah")
                .fetchSemanticsNodes().isNotEmpty()
        }
        // No Fajr a lista mostra só as gravações de Fajr: a comum não aparece.
        compose.onNodeWithTag("prayer_sound_option_makkah-mulla").assertDoesNotExist()
        compose.onNodeWithTag("prayer_sound_option_fajr-makkah")
            .performScrollTo()
            .performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("prayer_adhan_alarm").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag("prayer_adhan_alarm").performClick()
        compose.onNodeWithTag("prayer_adhan_day_1").performClick()
        compose.onNodeWithTag("prayer_adhan_save").performClick()

        compose.waitUntil(10_000) {
            runBlocking {
                settings.adhanAlarmByPrayer.first().contains(Prayer.FAJR) &&
                    settings.adhanSoundsByPrayer.first()[Prayer.FAJR] == AdhanSound.FAJR_MAKKAH &&
                    settings.adhanRepeatsByPrayer.first()[Prayer.FAJR]
                        ?.let { DayOfWeek.MONDAY !in it } == true
            }
        }

        val sounds = runBlocking { settings.adhanSoundsByPrayer.first() }
        val repeats = runBlocking { settings.adhanRepeatsByPrayer.first() }
        assertEquals(AdhanSound.FAJR_MAKKAH, sounds[Prayer.FAJR])
        assertFalse(Prayer.MAGHRIB in sounds.keys)
        assertFalse(DayOfWeek.MONDAY in repeats.getValue(Prayer.FAJR))
        assertTrue(DayOfWeek.TUESDAY in repeats.getValue(Prayer.FAJR))
    }

    @Test
    fun closingWithoutSavingDiscardsTheDraft() {
        reset()
        open(Prayer.DHUHR)

        compose.onNodeWithTag("prayer_adhan_notify").performClick()
        compose.onNodeWithTag("prayer_adhan_close").performClick()

        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("prayer_adhan_notify").fetchSemanticsNodes().isEmpty()
        }
        assertTrue(runBlocking { Prayer.DHUHR in settings.adhanPrayers.first() })
        assertTrue(runBlocking { settings.adhanAlarmByPrayer.first().isEmpty() })
    }

    @Test
    fun notifyOffSurvivesReopenAndTheBellShowsIt() {
        reset()
        open(Prayer.ISHA)

        compose.onNodeWithTag("prayer_adhan_notify").performClick()
        compose.onNodeWithTag("prayer_adhan_save").performClick()
        compose.waitUntil(10_000) {
            runBlocking { Prayer.ISHA !in settings.adhanPrayers.first() }
        }

        open(Prayer.ISHA)
        assertFalse(runBlocking { Prayer.ISHA in settings.adhanPrayers.first() })

        compose.onNodeWithTag("prayer_adhan_notify").performClick()
        compose.onNodeWithTag("prayer_adhan_save").performClick()
        compose.waitUntil(10_000) {
            runBlocking { Prayer.ISHA in settings.adhanPrayers.first() }
        }

        runBlocking {
            settings.setAdhanPrayers(AdhanPolicy.defaultEnabled)
            settings.setAdhanSoundsByPrayer(emptyMap())
            settings.setAdhanRemindersByPrayer(emptyMap())
            settings.setAdhanAlarmByPrayer(emptySet())
            settings.setAdhanRepeatsByPrayer(emptyMap())
        }
    }
}
