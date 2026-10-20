package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.farukzahra.muslimplus.prayer.Prayer

class PrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER)
        val prayer = runCatching { Prayer.valueOf(name.orEmpty()) }.getOrNull()

        if (prayer != null) {
            Notifications.ensureChannel(context)
            Notifications.showPrayer(context, prayer)
        }

        val pending = goAsync()
        PrayerScheduler(context).apply {
            // Reagenda a proxima oracao depois de disparar esta.
            Thread {
                try {
                    kotlinx.coroutines.runBlocking { rescheduleNow() }
                } finally {
                    pending.finish()
                }
            }.start()
        }
    }
}
