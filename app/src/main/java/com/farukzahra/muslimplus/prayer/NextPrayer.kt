package com.farukzahra.muslimplus.prayer

import com.farukzahra.muslimplus.location.City
import java.time.Instant
import java.time.ZoneId

data class NextPrayer(val prayer: Prayer, val at: Instant)

/**
 * Proxima oracao a partir de [now], virando o dia quando todas ja passaram.
 * Os offsets por oracao vem de fora para que o mesmo calculo usado na UI seja
 * o usado no alarme.
 */
object NextPrayerResolver {
    fun resolve(
        now: Instant,
        city: City,
        method: PrayerMethod,
        offsets: Map<Prayer, Int>,
    ): NextPrayer {
        val zone = ZoneId.of(city.zoneId)
        val today = now.atZone(zone).toLocalDate()
        val todaySchedule = PrayerEngine.schedule(
            today, city.latitude, city.longitude, zone, method, offsets,
        )
        val next = todaySchedule.nextAfter(now)
        if (next != null) return NextPrayer(next, todaySchedule.timeOf(next)!!)

        val tomorrow = PrayerEngine.schedule(
            today.plusDays(1), city.latitude, city.longitude, zone, method, offsets,
        )
        return NextPrayer(Prayer.FAJR, tomorrow.timeOf(Prayer.FAJR)!!)
    }
}
