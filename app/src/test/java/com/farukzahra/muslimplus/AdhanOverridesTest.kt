package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.adhan.AdhanOverrides
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanOverridesTest {
    @Test
    fun soundsRoundTripKeepingTheRightGroupPerPrayer() {
        val overrides = mapOf(
            Prayer.FAJR to AdhanSound.FAJR_MAKKAH,
            Prayer.MAGHRIB to AdhanSound.MAKKAH_MULLA,
        )
        val raw = AdhanOverrides.encodeSounds(overrides)
        assertEquals("FAJR=fajr-makkah;MAGHRIB=makkah-mulla", raw)
        assertEquals(overrides, AdhanOverrides.decodeSounds(raw))
    }

    @Test
    fun soundsDropSunriseUnknownIdsAndWrongGroup() {
        val raw = "SUNRISE=classic;FAJR=classic;DHUHR=madinah-nafman;ASR=nao-existe;ISHA=aqsa"
        assertEquals(
            mapOf(Prayer.ISHA to AdhanSound.AQSA),
            AdhanOverrides.decodeSounds(raw),
        )
        assertTrue(AdhanOverrides.decodeSounds(null).isEmpty())
        assertTrue(AdhanOverrides.decodeSounds("").isEmpty())
        assertTrue(AdhanOverrides.decodeSounds("lixo").isEmpty())
    }

    @Test
    fun remindersRoundTripAndPreserveOff() {
        val overrides = mapOf(Prayer.FAJR to 15, Prayer.ISHA to 0)
        val raw = AdhanOverrides.encodeReminders(overrides)
        assertEquals("FAJR=15;ISHA=0", raw)
        assertEquals(overrides, AdhanOverrides.decodeReminders(raw))
        assertEquals(
            mapOf(Prayer.FAJR to 10),
            AdhanOverrides.decodeReminders("FAJR=10;DHUHR=7;ASR=-5"),
        )
    }

    @Test
    fun alarmRoundTripDropsSunriseAndUnknown() {
        val raw = AdhanOverrides.encodeAlarm(
            setOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.MAGHRIB),
        )
        assertEquals("FAJR;MAGHRIB", raw)
        assertEquals(setOf(Prayer.FAJR, Prayer.MAGHRIB), AdhanOverrides.decodeAlarm(raw))
        assertEquals(setOf(Prayer.ISHA), AdhanOverrides.decodeAlarm("ISHA;SUNRISE;TYPO"))
        assertTrue(AdhanOverrides.decodeAlarm(null).isEmpty())
    }

    @Test
    fun repeatsRoundTripOnlyStoresPrayersThatDifferFromAllDays() {
        val all = AdhanOverrides.allDays
        val overrides = mapOf(
            Prayer.FAJR to setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            Prayer.ISHA to all,
        )
        val raw = AdhanOverrides.encodeRepeats(overrides)
        assertEquals("FAJR=1,5", raw)
        // "Todos os dias" não é gravado; quem herda volta como todos.
        assertEquals(
            mapOf(Prayer.FAJR to setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)),
            AdhanOverrides.decodeRepeats(raw),
        )
        assertEquals(
            mapOf(Prayer.ISHA to all),
            AdhanOverrides.decodeRepeats("ISHA=1,2,3,4,5,6,7"),
        )
    }

    @Test
    fun repeatsTreatEmptyOrInvalidAsEveryDay() {
        assertEquals(
            mapOf(Prayer.FAJR to AdhanOverrides.allDays),
            AdhanOverrides.decodeRepeats("FAJR="),
        )
        assertEquals(
            mapOf(Prayer.FAJR to AdhanOverrides.allDays),
            AdhanOverrides.decodeRepeats("FAJR=9,0,abc"),
        )
        assertEquals(
            mapOf(Prayer.ISHA to setOf(DayOfWeek.SUNDAY)),
            AdhanOverrides.decodeRepeats("ISHA=7;SUNRISE=1"),
        )
        assertTrue(AdhanOverrides.decodeRepeats(null).isEmpty())
    }

    @Test
    fun repeatsIgnoreSunriseEntries() {
        val decoded = AdhanOverrides.decodeRepeats("SUNRISE=1,2")
        assertFalse(Prayer.SUNRISE in decoded.keys)
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun malformedPairsAreSkipped() {
        assertEquals(
            mapOf(Prayer.ASR to AdhanSound.CLASSIC),
            AdhanOverrides.decodeSounds(";;FAJR;=;ASR=classic;"),
        )
        assertEquals(
            mapOf(Prayer.ASR to 5),
            AdhanOverrides.decodeReminders(";ASR=5;=10"),
        )
    }
}
