package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerOffsetsTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val date = LocalDate.of(2026, 10, 20)

    @Test
    fun emptyMapEncodesNothingAndDecodesToAllZeros() {
        assertEquals("", PrayerOffsets.encode(emptyMap()))
        assertEquals(PrayerOffsets.defaults, PrayerOffsets.decode(""))
        assertEquals(PrayerOffsets.defaults, PrayerOffsets.decode(null))
        Prayer.entries.forEach { prayer ->
            assertEquals(0, PrayerOffsets.decode("")[prayer])
        }
    }

    @Test
    fun negativeValuesRoundTrip() {
        val encoded = PrayerOffsets.encode(mapOf(Prayer.FAJR to 5, Prayer.ASR to -2))
        assertEquals("FAJR=5,ASR=-2", encoded)

        val decoded = PrayerOffsets.decode(encoded)
        assertEquals(5, decoded[Prayer.FAJR])
        assertEquals(-2, decoded[Prayer.ASR])
        assertEquals(0, decoded[Prayer.DHUHR])
        assertEquals(0, decoded[Prayer.ISHA])
    }

    @Test
    fun fullRangeRoundTrip() {
        val source = mapOf(
            Prayer.FAJR to -30,
            Prayer.DHUHR to 30,
            Prayer.ASR to -7,
            Prayer.MAGHRIB to 3,
            Prayer.ISHA to 1,
        )
        assertEquals(
            source,
            PrayerOffsets.decode(PrayerOffsets.encode(source))
                .filterKeys { it != Prayer.SUNRISE },
        )
        assertEquals(0, PrayerOffsets.decode(PrayerOffsets.encode(source))[Prayer.SUNRISE])
    }

    @Test
    fun corruptedStringFallsBackToZeroWithoutThrowing() {
        val decoded = PrayerOffsets.decode(
            "FAJR=abc, DHUHR=7, XX=4, ASR=, MAGHRIB=999, , ISHA=-999",
        )
        assertEquals(0, decoded[Prayer.FAJR])
        assertEquals(7, decoded[Prayer.DHUHR])
        assertEquals(0, decoded[Prayer.ASR])
        assertEquals(PrayerOffsets.MAX_MINUTES, decoded[Prayer.MAGHRIB])
        assertEquals(PrayerOffsets.MIN_MINUTES, decoded[Prayer.ISHA])

        assertEquals(PrayerOffsets.defaults, PrayerOffsets.decode("lixo sem igual"))
        assertEquals(PrayerOffsets.defaults, PrayerOffsets.decode("=,=,="))
    }

    /**
     * adhan monta cada horario num Calendar sem zerar MILLISECOND, entao duas
     * chamadas seguidas podem diferir em 1 ms. Comparar em milissegundos
     * exatos e' instavel; o contrato real do app e' o minuto.
     */
    private fun minutesBetween(from: java.time.Instant, to: java.time.Instant): Long =
        Math.round(Duration.between(from, to).toMillis() / 60_000.0)

    @Test
    fun plusFiveShiftsScheduleByExactlyFiveMinutes() {
        val base = PrayerEngine.schedule(date, -23.5505, -46.6333, zone)
        val shifted = PrayerEngine.schedule(
            date, -23.5505, -46.6333, zone,
            offsets = PrayerOffsets.decode("ISHA=5"),
        )

        assertEquals(
            5L,
            minutesBetween(base.times.getValue(Prayer.ISHA), shifted.times.getValue(Prayer.ISHA)),
        )
        assertEquals(
            0L,
            minutesBetween(base.times.getValue(Prayer.FAJR), shifted.times.getValue(Prayer.FAJR)),
        )
    }

    @Test
    fun minusThirtyShiftsScheduleBackwards() {
        val base = PrayerEngine.schedule(date, -23.5505, -46.6333, zone)
        val shifted = PrayerEngine.schedule(
            date, -23.5505, -46.6333, zone,
            offsets = PrayerOffsets.decode("FAJR=-30"),
        )
        val diff = minutesBetween(
            base.times.getValue(Prayer.FAJR),
            shifted.times.getValue(Prayer.FAJR),
        )
        assertTrue("esperado -30, obtido $diff", diff == -30L)
    }

    @Test
    fun encodeClampsValuesOutsideTheRange() {
        assertEquals("FAJR=30", PrayerOffsets.encode(mapOf(Prayer.FAJR to 999)))
        assertEquals("FAJR=-30", PrayerOffsets.encode(mapOf(Prayer.FAJR to -999)))
    }
}
