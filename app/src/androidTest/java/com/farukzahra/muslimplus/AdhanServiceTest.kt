package com.farukzahra.muslimplus

import android.app.ActivityManager
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanService
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** O servico sobe em primeiro plano e pode ser encerrado sem deixar audio preso. */
@RunWith(AndroidJUnit4::class)
class AdhanServiceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun startsForegroundPlaybackAndStops() {
        val activity = compose.activity
        AdhanService.start(activity, Prayer.DHUHR, AdhanSound.CLASSIC)

        val manager = activity.getSystemService(ActivityManager::class.java)
        val deadline = System.currentTimeMillis() + 10_000
        var foreground = false
        while (!foreground && System.currentTimeMillis() < deadline) {
            foreground = manager.getRunningServices(Int.MAX_VALUE).any {
                it.service.className == AdhanService::class.java.name && it.foreground
            }
            if (!foreground) Thread.sleep(100)
        }
        assertTrue("servico do adhan nao ficou em primeiro plano", foreground)

        // Deixa o sistema registrar o startForeground antes de pedir a parada.
        Thread.sleep(300)
        activity.stopService(Intent(activity, AdhanService::class.java))
        val stopDeadline = System.currentTimeMillis() + 5_000
        var stillRunning = true
        while (stillRunning && System.currentTimeMillis() < stopDeadline) {
            stillRunning = manager.getRunningServices(Int.MAX_VALUE)
                .any { it.service.className == AdhanService::class.java.name }
            if (stillRunning) Thread.sleep(100)
        }
        assertTrue("servico do adhan nao parou", !stillRunning)
    }
}
