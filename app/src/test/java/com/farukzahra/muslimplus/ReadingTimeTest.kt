package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.ReadingTime
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingTimeTest {
    private val today = LocalDate.of(2026, 10, 31)

    @Test
    fun roundsDownToMinutesAndIgnoresEmptyDays() {
        val days = mapOf(today to 125)
        assertEquals(2, ReadingTime.minutes(days, today))
        assertEquals(0, ReadingTime.minutes(days, today.minusDays(1)))
        assertEquals("2026-10-31=125", ReadingTime.encode(days))
        assertEquals(days, ReadingTime.decode("2026-10-31=125"))
    }

    @Test
    fun addAccumulatesAndCapsTheDay() {
        val once = ReadingTime.add(emptyMap(), today, 15)
        val twice = ReadingTime.add(once, today, 15)
        assertEquals(30, twice[today])
        assertEquals(0, ReadingTime.add(twice, today, 0)[today]?.minus(30) ?: -1)

        val capped = ReadingTime.add(mapOf(today to ReadingTime.MAX_SECONDS_PER_DAY - 5), today, 60)
        assertEquals(ReadingTime.MAX_SECONDS_PER_DAY, capped[today])
    }

    @Test
    fun decodeDropsGarbageAndClamps() {
        assertEquals(emptyMap<LocalDate, Int>(), ReadingTime.decode(null))
        assertEquals(emptyMap<LocalDate, Int>(), ReadingTime.decode("x=10;2026-10-31=abc;=4"))
        assertEquals(
            mapOf(today to 90),
            ReadingTime.decode("2026-10-31=90;2026-10-31=0"),
        )
        assertEquals(
            mapOf(today to ReadingTime.MAX_SECONDS_PER_DAY),
            ReadingTime.decode("2026-10-31=${ReadingTime.MAX_SECONDS_PER_DAY + 1000}"),
        )
    }

    @Test
    fun streakKeepsYesterdayWhenTodayHasNoReadingYet() {
        val days = mapOf(
            today.minusDays(1) to 90,
            today.minusDays(2) to 3600,
            today.minusDays(4) to 120,
        )
        assertEquals(2, ReadingTime.streak(days, today))
        assertEquals(3, ReadingTime.streak(days + (today to 60), today))
        assertEquals(0, ReadingTime.streak(emptyMap(), today))
    }

    @Test
    fun pruneKeepsRetentionWindow() {
        val days = mapOf(
            today.minusDays(ReadingTime.RETENTION_DAYS.toLong()) to 60,
            today.minusDays(ReadingTime.RETENTION_DAYS - 1L) to 60,
            today to 60,
        )
        val pruned = ReadingTime.prune(days, today)
        assertFalse(today.minusDays(ReadingTime.RETENTION_DAYS.toLong()) in pruned)
        assertTrue(today.minusDays(ReadingTime.RETENTION_DAYS - 1L) in pruned)
        assertTrue(today in pruned)
    }
}
