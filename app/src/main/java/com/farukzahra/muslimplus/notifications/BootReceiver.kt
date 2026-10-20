package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        Thread {
            try {
                runBlocking { PrayerScheduler(context).rescheduleNow() }
            } finally {
                pending.finish()
            }
        }.start()
    }
}
