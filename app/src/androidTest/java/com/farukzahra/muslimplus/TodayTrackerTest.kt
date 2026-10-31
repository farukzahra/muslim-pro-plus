package com.farukzahra.muslimplus

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.prayer.Prayer
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Rastreador do Hoje: marcar uma oração, marcar todas e sino por oração. */
@RunWith(AndroidJUnit4::class)
class TodayTrackerTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val settings = ServiceLocator.settings
    private val log = ServiceLocator.prayerLog

    @Test
    fun trackerMarksPrayersAndTogglesAdhan() {
        val today = runBlocking {
            val city = ServiceLocator.cities.byId(settings.cityId.first())
            LocalDate.now(ZoneId.of(city.zoneId))
        }
        val originalAdhan = runBlocking { settings.adhanPrayers.first() }
        runBlocking {
            settings.setOnboardingDone()
            log.setAll(today, done = false)
            settings.setAdhanPrayers(AdhanPolicy.eligible.toSet())
        }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        try {
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("tracker_card").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("tracker_week").assertExists()
            compose.onNodeWithTag("tracker_hint").assertExists()
            assertEquals(context.getString(R.string.today_tracker_progress, 0, 5), progress())

            compose.onNodeWithTag("prayer_check_fajr").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                progress() == context.getString(R.string.today_tracker_progress, 1, 5)
            }
            assertEquals(1, runBlocking { log.days.first()[today].orEmpty().size })

            // A linha inteira também marca: o alvo do círculo não é o único caminho.
            compose.onNodeWithTag("prayer_row_dhuhr").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                progress() == context.getString(R.string.today_tracker_progress, 2, 5)
            }

            compose.onNodeWithTag("tracker_mark_all").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                progress() == context.getString(R.string.today_tracker_progress, 5, 5)
            }
            compose.onNodeWithTag("tracker_done").performScrollTo().assertIsDisplayed()
            assertEquals(
                Prayer.values().toSet() - Prayer.SUNRISE,
                runBlocking { log.days.first()[today].orEmpty() },
            )

            // O sino desliga e religa a mesma preferência usada pelo agendador.
            compose.onNodeWithTag("prayer_bell_isha").performScrollTo().performClick()
            compose.waitUntil(5_000) {
                runBlocking { Prayer.ISHA !in settings.adhanPrayers.first() }
            }
            assertTrue(runBlocking { Prayer.ISHA !in settings.adhanPrayers.first() })
            compose.onNodeWithTag("prayer_bell_isha").performClick()
            compose.waitUntil(5_000) {
                runBlocking { Prayer.ISHA in settings.adhanPrayers.first() }
            }
        } finally {
            runBlocking {
                log.setAll(today, done = false)
                settings.setAdhanPrayers(originalAdhan)
            }
        }
    }

    private fun progress(): String =
        compose.onNodeWithTag("tracker_progress")
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString("") { it.text }
}
