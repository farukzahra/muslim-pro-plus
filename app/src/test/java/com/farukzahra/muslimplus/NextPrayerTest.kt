package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.NextPrayerResolver
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class NextPrayerTest {
    private val city = CityCatalog.byId("sao-paulo")
    private val zone = ZoneId.of(city.zoneId)

    private fun instantAt(hour: Int, minute: Int): Instant =
        LocalDate.of(2026, 10, 20).atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()

    @Test
    fun morningResolvesToDhuhr() {
        val next = NextPrayerResolver.resolve(
            instantAt(10, 0), city, PrayerMethod.MWL, PrayerOffsets.defaults,
        )
        assertEquals(Prayer.DHUHR, next.prayer)
    }

    @Test
    fun afterIshaResolvesToTomorrowFajr() {
        val next = NextPrayerResolver.resolve(
            instantAt(23, 0), city, PrayerMethod.MWL, PrayerOffsets.defaults,
        )
        assertEquals(Prayer.FAJR, next.prayer)
        assertEquals(
            LocalDate.of(2026, 10, 21),
            next.at.atZone(zone).toLocalDate(),
        )
    }

    @Test
    fun resolverAppliesTheConfiguredOffsets() {
        val now = instantAt(10, 0)
        val without = NextPrayerResolver.resolve(
            now, city, PrayerMethod.MWL, PrayerOffsets.defaults,
        )
        val shifted = NextPrayerResolver.resolve(
            now, city, PrayerMethod.MWL, PrayerOffsets.decode("DHUHR=5"),
        )
        assertEquals(Prayer.DHUHR, shifted.prayer)
        // adhan pode variar 1 ms entre chamadas; o contrato e' o minuto.
        val minutes = Math.round(Duration.between(without.at, shifted.at).toMillis() / 60_000.0)
        assertEquals(5L, minutes)
    }
}
