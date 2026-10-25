package com.farukzahra.muslimplus

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.farukzahra.muslimplus.adhan.AdhanService
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** O servico sobe, pode ser parado pelo app ou pela acao da notificacao. */
@RunWith(AndroidJUnit4::class)
class AdhanServiceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun startsForegroundPlaybackAndStops() {
        val activity = compose.activity
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            assertTrue("servico do adhan nao iniciou", waitUntil { AdhanService.playing.value })
            assertTrue(
                "servico do adhan nao ficou em primeiro plano",
                waitUntil { running(activity) },
            )
        } finally {
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
        assertTrue("servico do adhan nao parou", waitUntil { !running(activity) })
    }

    @Test
    fun inAppBannerStopsPlayback() {
        runBlocking { ServiceLocator.settings.setOnboardingDone() }
        compose.activityRule.scenario.recreate()
        val activity = compose.activity
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            compose.waitUntil(10_000) { AdhanService.playing.value }
            compose.onNodeWithTag("adhan_playing_banner").assertIsDisplayed()
            compose.onNodeWithTag("adhan_stop_button").performClick()
            compose.waitUntil(10_000) { !AdhanService.playing.value }
            compose.onNodeWithTag("adhan_playing_banner").assertDoesNotExist()
        } finally {
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
    }

    @Test
    fun notificationStopActionStopsPlayback() {
        val activity = compose.activity
        if (Build.VERSION.SDK_INT >= 33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                activity.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            assertTrue("chamado nao iniciou", waitUntil { AdhanService.playing.value })
            val manager = activity.getSystemService(NotificationManager::class.java)
            var posted: StatusBarNotification? = null
            assertTrue(
                "notificacao do adhan nao apareceu",
                waitUntil {
                    posted = manager.activeNotifications.firstOrNull {
                        it.id == AdhanService.NOTIFICATION_ID
                    }
                    posted != null
                },
            )
            val notification = posted!!.notification
            val stop = notification.actions.firstOrNull {
                it.title == activity.getString(R.string.adhan_stop)
            }
            assertNotNull("acao Parar ausente na notificacao", stop)
            stop!!.actionIntent.send()

            assertTrue(
                "acao da notificacao nao parou o audio",
                waitUntil { !AdhanService.playing.value },
            )
        } finally {
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
    }

    private fun running(activity: MainActivity): Boolean =
        activity.getSystemService(ActivityManager::class.java)
            .getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == AdhanService::class.java.name && it.foreground }

    private fun waitUntil(
        timeoutMs: Long = 10_000,
        condition: () -> Boolean,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(100)
        }
        return condition()
    }
}
