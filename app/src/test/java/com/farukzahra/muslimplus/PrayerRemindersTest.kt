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

    @Test
    fun minutesPerPrayerOverrideTheGlobalValue() {
        val overrides = mapOf(Prayer.FAJR to 30, Prayer.ISHA to PrayerReminder.OFF)
        assertEquals(30, PrayerReminder.minutesFor(Prayer.FAJR, 5, overrides))
        assertEquals(PrayerReminder.OFF, PrayerReminder.minutesFor(Prayer.ISHA, 5, overrides))
        assertEquals(5, PrayerReminder.minutesFor(Prayer.DHUHR, 5, overrides))
        assertEquals(
            PrayerReminder.OFF,
            PrayerReminder.minutesFor(Prayer.DHUHR, 5, mapOf(Prayer.DHUHR to 7)),
        )
    }

    @Test
    fun nextUsesTheMinutesOfEachPrayer() {
        val fajr = PrayerEngine.schedule(
            date, city.latitude, city.longitude, zone, PrayerMethod.MWL, PrayerOffsets.defaults,
        ).timeOf(Prayer.FAJR)!!
        val minutes = { prayer: Prayer -> if (prayer == Prayer.FAJR) 10 else 0 }

        val reminder = PrayerReminder.next(
            fajr.minusSeconds(3_600), city, PrayerMethod.MWL, PrayerOffsets.defaults, minutes,
        )

        assertEquals(Prayer.FAJR, reminder?.prayer)
        // O motor recalcula o horário a cada chamada; diferenças de milissegundos
        // fazem 599,998 s virarem 9 minutos inteiros. Compara com folga.
        val gap = Duration.between(reminder!!.at, fajr).seconds
        assertTrue("gap=$gap", gap in 598L..602L)
    }

    @Test
    fun prayersTurnedOffDoNotProduceAReminder() {
        assertNull(
            PrayerReminder.next(
                at(4, 0), city, PrayerMethod.MWL, PrayerOffsets.defaults, { PrayerReminder.OFF },
            ),
        )
    }
}
