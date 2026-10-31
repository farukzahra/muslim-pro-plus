package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerLog
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerLogTest {
    private val day = LocalDate.of(2026, 10, 31)
    private val all = PrayerLog.trackable.toSet()

    @Test
    fun sunriseIsNeverTracked() {
        val done = PrayerLog.sanitize(setOf("FAJR", "SUNRISE", "BOGUS"))
        assertEquals(setOf(Prayer.FAJR), done)
        assertFalse("SUNRISE" in PrayerLog.encode(all + Prayer.SUNRISE))
    }

    @Test
    fun progressCountsOnlyTheFivePrayers() {
        assertEquals(0, PrayerLog.progress(emptySet()))
        assertEquals(5, PrayerLog.progress(all))
        assertEquals(3, PrayerLog.progress(setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ISHA)))
        assertFalse(PrayerLog.isComplete(all - Prayer.ISHA))
        assertTrue(PrayerLog.isComplete(all))
    }

    @Test
    fun codecRoundTripsAndIgnoresGarbage() {
        val days = mapOf(
            day to setOf(Prayer.FAJR, Prayer.ISHA),
            day.plusDays(1) to all,
            day.plusDays(2) to emptySet(),
        )
        val encoded = PrayerLog.encodeDays(days)
        assertEquals(
            mapOf(
                day to setOf(Prayer.FAJR, Prayer.ISHA),
                day.plusDays(1) to all,
            ),
            PrayerLog.decodeDays(encoded),
        )

        assertEquals(emptyMap<LocalDate, Set<Prayer>>(), PrayerLog.decodeDays(null))
        assertEquals(emptyMap<LocalDate, Set<Prayer>>(), PrayerLog.decodeDays("  "))
        assertEquals(
            mapOf(day to setOf(Prayer.DHUHR)),
            PrayerLog.decodeDays("nao-e-data=FAJR;2026-10-31=SUNRISE,DHUHR;2026-10-31=X"),
        )
    }

    @Test
    fun weekHasSevenDaysEndingToday() {
        val week = PrayerLog.week(
            end = day,
            days = mapOf(day.minusDays(1) to all),
            length = 7,
        )
        assertEquals(7, week.size)
        assertEquals(day.minusDays(6), week.first().date)
        assertEquals(day, week.last().date)
        assertTrue(week[5].complete)
        assertFalse(week.last().complete)
        assertEquals(0, PrayerLog.week(day, emptyMap(), length = 0).size)
    }

    @Test
    fun streakKeepsYesterdayWhenTodayIsStillOpen() {
        val days = mapOf(
            day to setOf(Prayer.FAJR),
            day.minusDays(1) to all,
            day.minusDays(2) to all,
            day.minusDays(4) to all,
        )
        assertEquals(2, PrayerLog.streak(days, day))

        val completedToday = days + (day to all)
        assertEquals(3, PrayerLog.streak(completedToday, day))
        assertEquals(0, PrayerLog.streak(emptyMap(), day))
    }

    @Test
    fun pruneDropsOldAndFutureDays() {
        val days = mapOf(
            day.minusDays(PrayerLog.RETENTION_DAYS.toLong()) to all,
            day.minusDays((PrayerLog.RETENTION_DAYS - 1).toLong()) to all,
            day to all,
            day.plusDays(2) to all,
        )
        val pruned = PrayerLog.prune(days, day)
        assertFalse(day.minusDays(PrayerLog.RETENTION_DAYS.toLong()) in pruned)
        assertTrue(day.minusDays(PrayerLog.RETENTION_DAYS - 1L) in pruned)
        assertTrue(day in pruned)
        assertFalse(day.plusDays(2) in pruned)
    }
}
