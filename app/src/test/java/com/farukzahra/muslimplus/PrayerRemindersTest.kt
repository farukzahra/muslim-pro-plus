package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerRemindersTest {
    private val catalog = CityCatalog.fromLines(
        File("src/main/assets/cities.txt").readLines(Charsets.UTF_8),
    )
    private val city = catalog.defaultForZone("America/Sao_Paulo")!!
    private val zone = ZoneId.of(city.zoneId)
    private val date = LocalDate.of(2026, 10, 31)

    private fun at(hour: Int, minute: Int): Instant =
        date.atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()

    @Test
    fun onlyListedMinutesAreAccepted() {
        assertEquals(PrayerReminder.OFF, PrayerReminder.sanitize(null))
        assertEquals(PrayerReminder.OFF, PrayerReminder.sanitize(7))
        assertEquals(10, PrayerReminder.sanitize(10))
        assertTrue(PrayerReminder.isOff(null))
        assertTrue(PrayerReminder.isOff(0))
    }

    @Test
    fun offNeverSchedulesAnything() {
        assertNull(
            PrayerReminder.next(at(4, 0), city, PrayerMethod.MWL, PrayerOffsets.defaults, 0),
        )
    }

    @Test
    fun reminderFiresBeforeTheNextPrayer() {
        val now = at(0, 10)
        val minutes = 10
        val reminder = PrayerReminder.next(
            now, city, PrayerMethod.MWL, PrayerOffsets.defaults, minutes,
        )
        val fajr = PrayerEngine.schedule(
            date, city.latitude, city.longitude, zone, PrayerMethod.MWL, PrayerOffsets.defaults,
        ).timeOf(Prayer.FAJR)
        assertEquals(Prayer.FAJR, reminder?.prayer)
        assertEquals(10L, Duration.between(reminder!!.at, fajr!!).toMinutes())
        assertTrue(reminder.at.isAfter(now))
    }

    @Test
    fun whenTheWindowAlreadyPassedItMovesToTheNextPrayer() {
        val fajr = PrayerEngine.schedule(
            date, city.latitude, city.longitude, zone, PrayerMethod.MWL, PrayerOffsets.defaults,
        ).timeOf(Prayer.FAJR)!!
        // 2 minutos antes do Fajr, o aviso de 10 min já passou: o próximo é o Dhuhr.
        val reminder = PrayerReminder.next(
            fajr.minusSeconds(120), city, PrayerMethod.MWL, PrayerOffsets.defaults, 10,
        )
        assertEquals(Prayer.DHUHR, reminder?.prayer)
        assertTrue(reminder!!.at.isAfter(fajr.minusSeconds(120)))
    }

    @Test
    fun afterIshaTheReminderIsForTomorrowFajr() {
        val reminder = PrayerReminder.next(
            at(23, 30), city, PrayerMethod.MWL, PrayerOffsets.defaults, 15,
        )
        assertEquals(Prayer.FAJR, reminder?.prayer)
        assertEquals(date.plusDays(1), reminder!!.at.atZone(zone).toLocalDate())
    }
}
