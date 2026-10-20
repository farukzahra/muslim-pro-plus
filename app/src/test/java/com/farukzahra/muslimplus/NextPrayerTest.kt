package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.NextPrayerResolver
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerMethod
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
        val next = NextPrayerResolver.resolve(instantAt(10, 0), city, PrayerMethod.MWL)
        assertEquals(Prayer.DHUHR, next.prayer)
    }

    @Test
    fun afterIshaResolvesToTomorrowFajr() {
        val next = NextPrayerResolver.resolve(instantAt(23, 0), city, PrayerMethod.MWL)
        assertEquals(Prayer.FAJR, next.prayer)
        assertEquals(
            LocalDate.of(2026, 10, 21),
            next.at.atZone(zone).toLocalDate(),
        )
    }
}
