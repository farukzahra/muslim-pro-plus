package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerMethod
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerEngineTest {
    private fun local(
        schedule: com.farukzahra.muslimplus.prayer.PrayerSchedule,
        prayer: Prayer,
        zone: ZoneId,
    ): LocalTime = schedule.times.getValue(prayer).atZone(zone).toLocalTime()

    private fun assertClose(expected: LocalTime, actual: LocalTime, label: String) {
        val diff = Duration.between(expected, actual).abs().toMinutes()
        assertTrue("$label: esperado $expected, obtido $actual", diff <= 2)
    }

    @Test
    fun saoPauloMatchesReference() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val schedule = PrayerEngine.schedule(
            LocalDate.of(2026, 10, 20), -23.5505, -46.6333, zone, PrayerMethod.MWL,
        )
        assertClose(LocalTime.of(4, 11), local(schedule, Prayer.FAJR, zone), "fajr")
        assertClose(LocalTime.of(5, 29), local(schedule, Prayer.SUNRISE, zone), "sunrise")
        assertClose(LocalTime.of(11, 51), local(schedule, Prayer.DHUHR, zone), "dhuhr")
        assertClose(LocalTime.of(15, 18), local(schedule, Prayer.ASR, zone), "asr")
        assertClose(LocalTime.of(18, 14), local(schedule, Prayer.MAGHRIB, zone), "maghrib")
        assertClose(LocalTime.of(19, 27), local(schedule, Prayer.ISHA, zone), "isha")
    }

    @Test
    fun makkahUsesUmmAlQuraInterval() {
        val zone = ZoneId.of("Asia/Riyadh")
        val schedule = PrayerEngine.schedule(
            LocalDate.of(2026, 10, 20), 21.4225, 39.8262, zone, PrayerMethod.MAKKAH,
        )
        assertClose(LocalTime.of(5, 2), local(schedule, Prayer.FAJR, zone), "fajr")
        assertClose(LocalTime.of(12, 5), local(schedule, Prayer.DHUHR, zone), "dhuhr")
        assertClose(LocalTime.of(17, 52), local(schedule, Prayer.MAGHRIB, zone), "maghrib")
        assertClose(LocalTime.of(19, 22), local(schedule, Prayer.ISHA, zone), "isha")
    }

    @Test
    fun londonSummerUsesHighLatitudeRule() {
        val zone = ZoneId.of("Europe/London")
        val schedule = PrayerEngine.schedule(
            LocalDate.of(2026, 6, 21), 51.5074, -0.1278, zone, PrayerMethod.MWL,
        )
        assertClose(LocalTime.of(2, 31), local(schedule, Prayer.FAJR, zone), "fajr")
        assertClose(LocalTime.of(13, 2), local(schedule, Prayer.DHUHR, zone), "dhuhr")
        assertClose(LocalTime.of(21, 22), local(schedule, Prayer.MAGHRIB, zone), "maghrib")
        assertClose(LocalTime.of(23, 27), local(schedule, Prayer.ISHA, zone), "isha")
    }

    @Test
    fun offsetsShiftASinglePrayer() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val base = PrayerEngine.schedule(
            LocalDate.of(2026, 10, 20), -23.5505, -46.6333, zone,
        )
        val shifted = PrayerEngine.schedule(
            LocalDate.of(2026, 10, 20), -23.5505, -46.6333, zone,
            offsets = mapOf(Prayer.FAJR to 5),
        )
        assertEquals(
            5L,
            Duration.between(base.times.getValue(Prayer.FAJR), shifted.times.getValue(Prayer.FAJR))
                .toMinutes(),
        )
    }

    @Test
    fun nextPrayerSkipsSunrise() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val schedule = PrayerEngine.schedule(
            LocalDate.of(2026, 10, 20), -23.5505, -46.6333, zone,
        )
        val justAfterFajr = schedule.times.getValue(Prayer.FAJR).plusSeconds(60)
        assertEquals(Prayer.DHUHR, schedule.nextAfter(justAfterFajr))
    }
}
