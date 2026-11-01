package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.adhan.RingerState
import com.farukzahra.muslimplus.prayer.Prayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanPolicyTest {
    @Test
    fun defaultIncludesAllFivePrayersAndNeverIncludesSunrise() {
        assertEquals(
            setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA),
            AdhanPolicy.defaultEnabled,
        )
        assertTrue(Prayer.FAJR in AdhanPolicy.defaultEnabled)
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
        val enabled = AdhanPolicy.eligible.toSet() - Prayer.FAJR
        assertFalse(AdhanPolicy.shouldPlay(Prayer.FAJR, enabled, false, RingerState.Normal))
        assertFalse(AdhanPolicy.shouldPlay(Prayer.SUNRISE, enabled, false, RingerState.Normal))
        assertFalse(
            AdhanPolicy.shouldPlay(Prayer.DHUHR, emptySet(), false, RingerState.Normal),
        )
    }

    @Test
    fun fajrUsesItsOwnSoundOnlyWhenOverrideIsOn() {
        val global = AdhanSound.CLASSIC
        val fajr = AdhanSound.FAJR_MAKKAH

        assertEquals(fajr, AdhanPolicy.resolve(Prayer.FAJR, global, fajr, true))
        assertEquals(global, AdhanPolicy.resolve(Prayer.DHUHR, global, fajr, true))
        assertEquals(global, AdhanPolicy.resolve(Prayer.FAJR, global, fajr, false))
    }

    @Test
    fun resolveFallsBackWhenTheSoundBelongsToTheWrongGroup() {
        assertEquals(
            AdhanSound.defaultFajr,
            AdhanPolicy.resolve(Prayer.FAJR, AdhanSound.CLASSIC, AdhanSound.CLASSIC, true),
        )
        assertEquals(
            AdhanSound.default,
            AdhanPolicy.resolve(Prayer.DHUHR, AdhanSound.MADINAH_NAFMAN, AdhanSound.FAJR_MAKKAH, true),
        )
        assertEquals(
            AdhanSound.default,
            AdhanPolicy.resolve(Prayer.FAJR, AdhanSound.MADINAH_NAFMAN, AdhanSound.FAJR_MAKKAH, false),
        )
    }
}
