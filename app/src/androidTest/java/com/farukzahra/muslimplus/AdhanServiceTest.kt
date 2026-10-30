package com.farukzahra.muslimplus

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.ParcelFileDescriptor
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

    @Test
    fun volumeChangeStopsPlayback(
    ) {
        val activity = compose.activity
        val audio = activity.getSystemService(AudioManager::class.java)
        val original = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            assertTrue("chamado nao iniciou", waitUntil { AdhanService.playing.value })
            // O emulador ignora `input keyevent KEYCODE_VOLUME_UP` (o volume
            // nao muda), entao o teste mexe no stream como a tecla faria.
            audio.adjustStreamVolume(
                AudioManager.STREAM_ALARM,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI,
            )
            assertTrue(
                "mexer no volume nao parou o chamado",
                waitUntil { !AdhanService.playing.value },
            )
        } finally {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, original, 0)
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
    }

    @Test
    fun rotationStopsPlayback() {
        val activity = compose.activity
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            assertTrue("chamado nao iniciou", waitUntil { AdhanService.playing.value })
            shell("settings put system accelerometer_rotation 0")
            shell("settings put system user_rotation 1")
            assertTrue(
                "girar o aparelho nao parou o chamado",
                waitUntil { !AdhanService.playing.value },
            )
        } finally {
            shell("settings put system user_rotation 0")
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
    }

    @Test
    fun screenOffStopsPlayback() {
        val activity = compose.activity
        val stayOn = shellOutput("settings get global stay_on_while_plugged_in").trim()
        shell("settings put global stay_on_while_plugged_in 0")
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        try {
            assertTrue("chamado nao iniciou", waitUntil { AdhanService.playing.value })
            shell("input keyevent KEYCODE_POWER")
            assertTrue(
                "desligar a tela nao parou o chamado",
                waitUntil { !AdhanService.playing.value },
            )
        } finally {
            shell("input keyevent KEYCODE_WAKEUP")
            shell("wm dismiss-keyguard")
            shell("settings put global stay_on_while_plugged_in ${stayOn.ifBlank { "1" }}")
            shell("am start -n ${activity.packageName}/.MainActivity")
            activity.stopService(Intent(activity, AdhanService::class.java))
        }
    }

    private fun shellOutput(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use {
            it.readBytes().toString(Charsets.UTF_8)
        }
    }

    private fun shell(command: String) {
        shellOutput(command)
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
