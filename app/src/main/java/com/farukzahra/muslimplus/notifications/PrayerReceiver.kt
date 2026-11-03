package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanOverrides
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanService
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.adhan.RingerState
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER)
        val prayer = runCatching { Prayer.valueOf(name.orEmpty()) }.getOrNull()
        val reminder = intent.getBooleanExtra(PrayerScheduler.EXTRA_REMINDER, false)
        val reminderMinutes = intent.getIntExtra(PrayerScheduler.EXTRA_REMINDER_MINUTES, -1)

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
                                if (reminderMinutes >= 0) {
                                    reminderMinutes
                                } else {
                                    ServiceLocator.settings.adhanReminderMinutes.first()
                                },
                            )
                        }
                    } else if (prayer != null) {
                        val adhanOn = prayer in ServiceLocator.settings.adhanPrayers.first()
                        val playsToday = adhanOn && repeatsToday(prayer)
                        if (ServiceLocator.settings.notificationsEnabled.first() && playsToday) {
                            Notifications.ensureChannel(context)
                            Notifications.showPrayer(context, prayer)
                        }
                        if (adhanOn && playsToday) maybePlayAdhan(context, prayer)
                    }
                    PrayerScheduler(context).rescheduleNow()
                }
            } finally {
                pending.finish()
            }
        }.start()
    }

    /**
     * Dia da semana da repetição daquela oração, no fuso da cidade escolhida.
     * Sem entrada na preferência, todos os dias valem.
     */
    private suspend fun repeatsToday(prayer: Prayer): Boolean {
        val settings = ServiceLocator.settings
        val overrides = settings.adhanRepeatsByPrayer.first()
        val allowed = overrides[prayer] ?: AdhanOverrides.allDays
        val cityId = settings.cityId.first()
        val zone = runCatching { ZoneId.of(ServiceLocator.cities.byId(cityId).zoneId) }
            .getOrDefault(ZoneId.systemDefault())
        return Instant.now().atZone(zone).dayOfWeek in allowed
    }

    private suspend fun maybePlayAdhan(context: Context, prayer: Prayer) {
        val settings = ServiceLocator.settings
        val enabled = settings.adhanPrayers.first()
        val respectSilent = settings.adhanRespectSilent.first()
        val global = AdhanSound.findNormal(settings.adhanSoundId.first())
        val fajr = AdhanSound.findFajr(settings.adhanFajrSoundId.first())
        val override = settings.adhanFajrOverride.first()
        val sound = AdhanPolicy.resolveFor(
            prayer,
            global,
            fajr,
            override,
            settings.adhanSoundsByPrayer.first(),
        )
        val alarm = prayer in settings.adhanAlarmByPrayer.first()
        if (AdhanPolicy.shouldPlay(prayer, enabled, respectSilent, ringerState(context), alarm)) {
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
