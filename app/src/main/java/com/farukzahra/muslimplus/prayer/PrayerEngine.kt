package com.farukzahra.muslimplus.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

data class PrayerSchedule(
    val times: Map<Prayer, Instant>,
) {
    fun timeOf(prayer: Prayer): Instant? = times[prayer]

    /** Proxima oracao a partir de [now], considerando so o dia calculado. */
    fun nextAfter(now: Instant): Prayer? =
        Prayer.entries
            .filter { it != Prayer.SUNRISE }
            .filter { times[it]?.isAfter(now) == true }
            .minByOrNull { times.getValue(it) }
}

object PrayerEngine {
    fun schedule(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        method: PrayerMethod = PrayerMethod.MWL,
        offsets: Map<Prayer, Int> = emptyMap(),
    ): PrayerSchedule {
        val parameters = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        parameters.fajrAngle = method.fajrAngle
        parameters.ishaAngle = method.ishaAngle
        parameters.ishaInterval = method.ishaMinutes
        parameters.madhab = Madhab.SHAFI
        parameters.highLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE

        val noon = Date.from(date.atTime(12, 0).atZone(zone).toInstant())
        val times = PrayerTimes(
            Coordinates(latitude, longitude),
            DateComponents.from(noon),
            parameters,
        )

        fun shift(prayer: Prayer, value: Date?): Instant =
            value!!.toInstant().plusSeconds((offsets[prayer] ?: 0).toLong() * 60L)

        return PrayerSchedule(
            mapOf(
                Prayer.FAJR to shift(Prayer.FAJR, times.fajr),
                Prayer.SUNRISE to shift(Prayer.SUNRISE, times.sunrise),
                Prayer.DHUHR to shift(Prayer.DHUHR, times.dhuhr),
                Prayer.ASR to shift(Prayer.ASR, times.asr),
                Prayer.MAGHRIB to shift(Prayer.MAGHRIB, times.maghrib),
                Prayer.ISHA to shift(Prayer.ISHA, times.isha),
            ),
        )
    }
}
