package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanService
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.adhan.RingerState
import com.farukzahra.muslimplus.prayer.Prayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER)
        val prayer = runCatching { Prayer.valueOf(name.orEmpty()) }.getOrNull()
        val reminder = intent.getBooleanExtra(PrayerScheduler.EXTRA_REMINDER, false)

        val pending = goAsync()
        Thread {
            try {
                runBlocking {
                    if (prayer != null && reminder) {
                        if (ServiceLocator.settings.notificationsEnabled.first()) {
                            Notifications.ensureChannel(context)
                            Notifications.showReminder(
                                context,
                                prayer,
                                ServiceLocator.settings.adhanReminderMinutes.first(),
                            )
                        }
                    } else if (prayer != null) {
                        if (ServiceLocator.settings.notificationsEnabled.first()) {
                            Notifications.ensureChannel(context)
                            Notifications.showPrayer(context, prayer)
                        }
                        maybePlayAdhan(context, prayer)
                    }
                    PrayerScheduler(context).rescheduleNow()
                }
            } finally {
                pending.finish()
            }
        }.start()
    }

    private suspend fun maybePlayAdhan(context: Context, prayer: Prayer) {
        val settings = ServiceLocator.settings
        val enabled = settings.adhanPrayers.first()
        val respectSilent = settings.adhanRespectSilent.first()
        val global = AdhanSound.findNormal(settings.adhanSoundId.first())
        val fajr = AdhanSound.findFajr(settings.adhanFajrSoundId.first())
        val override = settings.adhanFajrOverride.first()
        val sound = AdhanPolicy.resolve(prayer, global, fajr, override)
        if (AdhanPolicy.shouldPlay(prayer, enabled, respectSilent, ringerState(context))) {
            AdhanService.start(context, prayer, sound)
        }
    }

    private fun ringerState(context: Context): RingerState =
        when (context.getSystemService(AudioManager::class.java).ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> RingerState.Silent
            AudioManager.RINGER_MODE_VIBRATE -> RingerState.Vibrate
            else -> RingerState.Normal
        }
}
