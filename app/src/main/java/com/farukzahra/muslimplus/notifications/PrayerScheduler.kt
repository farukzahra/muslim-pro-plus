package com.farukzahra.muslimplus.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.prayer.NextPrayerResolver
import com.farukzahra.muslimplus.prayer.PrayerMethod
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerScheduler(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun reschedule() {
        scope.launch { rescheduleNow() }
    }

    suspend fun rescheduleNow() {
        val settings = ServiceLocator.settings
        if (!settings.notificationsEnabled.first()) {
            cancel()
            return
        }
        val city = ServiceLocator.cities.byId(settings.cityId.first())
        val method = runCatching { PrayerMethod.valueOf(settings.methodName.first()) }
            .getOrDefault(PrayerMethod.MWL)
        val offsets = settings.prayerOffsets.first()
        val next = NextPrayerResolver.resolve(Instant.now(), city, method, offsets)

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(next.prayer)
        if (ExactAlarms.isAvailable(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, next.at.toEpochMilli(), pending,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, next.at.toEpochMilli(), pending,
            )
        }
    }

    fun cancel() {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(null))
    }

    private fun pendingIntent(prayer: com.farukzahra.muslimplus.prayer.Prayer?): PendingIntent {
        val intent = Intent(context, PrayerReceiver::class.java)
        if (prayer != null) intent.putExtra(EXTRA_PRAYER, prayer.name)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val REQUEST_CODE = 1001
    }
}
