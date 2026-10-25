package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.RingerState
import com.farukzahra.muslimplus.prayer.Prayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanPolicyTest {
    @Test
    fun defaultLeavesFajrOffAndNeverIncludesSunrise() {
        assertEquals(
            setOf(Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA),
            AdhanPolicy.defaultEnabled,
        )
        assertFalse(Prayer.FAJR in AdhanPolicy.defaultEnabled)
        assertFalse(Prayer.SUNRISE in AdhanPolicy.eligible)
    }

    @Test
    fun missingPreferenceUsesDefaultsButEmptyMeansAllOff() {
        assertEquals(AdhanPolicy.defaultEnabled, AdhanPolicy.sanitize(null))
        assertEquals(emptySet<Prayer>(), AdhanPolicy.sanitize(emptySet()))
    }

    @Test
    fun sanitizeDropsUnknownAndSunriseNames() {
        assertEquals(
            setOf(Prayer.FAJR, Prayer.ISHA),
            AdhanPolicy.sanitize(setOf("FAJR", "ISHA", "SUNRISE", "MAGHRIB_TYPO")),
        )
    }

    @Test
    fun encodeKeepsOnlyEligiblePrayers() {
        assertEquals(
            setOf("FAJR", "ISHA"),
            AdhanPolicy.encode(setOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.ISHA)),
        )
    }

    @Test
    fun silentIsIgnoredByDefaultAndRespectedWhenEnabled() {
        val enabled = AdhanPolicy.defaultEnabled
        assertTrue(AdhanPolicy.shouldPlay(Prayer.DHUHR, enabled, false, RingerState.Silent))
        assertTrue(AdhanPolicy.shouldPlay(Prayer.DHUHR, enabled, false, RingerState.Vibrate))
    }

    @Test
    fun respectSilentBlocksSilentAndVibrateOnly() {
        val enabled = AdhanPolicy.defaultEnabled
        assertFalse(AdhanPolicy.shouldPlay(Prayer.DHUHR, enabled, true, RingerState.Silent))
        assertFalse(AdhanPolicy.shouldPlay(Prayer.DHUHR, enabled, true, RingerState.Vibrate))
        assertTrue(AdhanPolicy.shouldPlay(Prayer.DHUHR, enabled, true, RingerState.Normal))
    }

    @Test
    fun disabledPrayerAndSunriseNeverPlay() {
        val enabled = AdhanPolicy.defaultEnabled
        assertFalse(AdhanPolicy.shouldPlay(Prayer.FAJR, enabled, false, RingerState.Normal))
        assertFalse(AdhanPolicy.shouldPlay(Prayer.SUNRISE, enabled, false, RingerState.Normal))
        assertFalse(
            AdhanPolicy.shouldPlay(Prayer.DHUHR, emptySet(), false, RingerState.Normal),
        )
    }
}
