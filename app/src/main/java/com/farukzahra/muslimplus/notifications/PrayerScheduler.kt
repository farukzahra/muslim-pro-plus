package com.farukzahra.muslimplus.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.prayer.NextPrayerResolver
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Dois alarmes independentes: o da oração (que pode tocar o adhan) e o do
 * lembrete, alguns minutos antes, que só mostra notificação. Cada um tem
 * request code próprio para um não cancelar o outro.
 */
class PrayerScheduler(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun reschedule() {
        scope.launch { rescheduleNow() }
    }

    suspend fun rescheduleNow() {
        val settings = ServiceLocator.settings
        val notifications = settings.notificationsEnabled.first()
        val hasAdhan = settings.adhanPrayers.first().isNotEmpty()
        val globalMinutes = settings.adhanReminderMinutes.first()
        val minuteOverrides = settings.adhanRemindersByPrayer.first()
        val minutesFor = { prayer: Prayer ->
            PrayerReminder.minutesFor(prayer, globalMinutes, minuteOverrides)
        }
        val wantsReminder = AdhanPolicy.eligible.any { !PrayerReminder.isOff(minutesFor(it)) }
        if (!notifications && !hasAdhan && !wantsReminder) {
            cancel()
            return
        }
        val cityId = settings.cityId.first()
        val city = ServiceLocator.cities.byId(cityId)
        val method = runCatching { PrayerMethod.valueOf(settings.methodName.first()) }
            .getOrDefault(PrayerMethod.MWL)
        val offsets = settings.prayerOffsets.first()

        if (notifications || hasAdhan) {
            val next = NextPrayerResolver.resolve(Instant.now(), city, method, offsets)
            setAlarm(next.at.toEpochMilli(), pendingIntent(next.prayer, reminder = false))
        } else {
            cancelPrayer()
        }

        val reminder = if (wantsReminder) {
            PrayerReminder.next(Instant.now(), city, method, offsets, minutesFor)
        } else {
            null
        }
        if (reminder != null) {
            setAlarm(
                reminder.at.toEpochMilli(),
                pendingIntent(
                    reminder.prayer,
                    reminder = true,
                    minutes = minutesFor(reminder.prayer),
                ),
            )
        } else {
            cancelReminder()
        }
    }

    fun cancel() {
        cancelPrayer()
        cancelReminder()
    }

    private fun setAlarm(atMillis: Long, pending: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (ExactAlarms.isAvailable(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        }
    }

    private fun cancelPrayer() {
        context.getSystemService(AlarmManager::class.java)
            .cancel(pendingIntent(null, reminder = false))
    }

    private fun cancelReminder() {
        context.getSystemService(AlarmManager::class.java)
            .cancel(pendingIntent(null, reminder = true))
    }

    private fun pendingIntent(
        prayer: Prayer?,
        reminder: Boolean,
        minutes: Int = -1,
    ): PendingIntent {
        val intent = Intent(context, PrayerReceiver::class.java)
        if (prayer != null) intent.putExtra(EXTRA_PRAYER, prayer.name)
        intent.putExtra(EXTRA_REMINDER, reminder)
        if (minutes >= 0) intent.putExtra(EXTRA_REMINDER_MINUTES, minutes)
        return PendingIntent.getBroadcast(
            context,
            if (reminder) REMINDER_REQUEST_CODE else PRAYER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_REMINDER = "reminder"
        const val EXTRA_REMINDER_MINUTES = "reminder_minutes"
        const val PRAYER_REQUEST_CODE = 1001
        const val REMINDER_REQUEST_CODE = 1002
    }
}
