package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.ReaderFont
import com.farukzahra.muslimplus.settings.ReaderMode
import com.farukzahra.muslimplus.settings.ReaderPrefs
import com.farukzahra.muslimplus.settings.ReaderTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPrefsTest {
    @Test
    fun unknownIdsFallBackToDefaults() {
        assertEquals(ReaderTheme.MODERN, ReaderTheme.fromId(null))
        assertEquals(ReaderTheme.MODERN, ReaderTheme.fromId("neon"))
        assertEquals(ReaderTheme.PAPER, ReaderTheme.fromId("paper"))
        assertEquals(ReaderFont.MEDIUM, ReaderFont.fromId("gigante"))
        assertEquals(ReaderMode.LIST, ReaderMode.fromId("scroll"))
        assertEquals(ReaderMode.PAGE, ReaderMode.fromId("page"))
    }

    @Test
    fun fontScalesIncreaseInOrder() {
        val scales = ReaderPrefs.fonts.map { it.scale }
        assertEquals(scales.sorted(), scales)
        assertTrue(ReaderPrefs.fonts.all { it.scale >= 1f })
    }

    @Test
    fun speedCyclesOnlyThroughAcceptedValues() {
        assertEquals(1f, ReaderPrefs.speed(null))
        assertEquals(1f, ReaderPrefs.speed(0.3f))
        assertEquals(0.75f, ReaderPrefs.speed(0.75f))
        assertEquals(1.25f, ReaderPrefs.nextSpeed(1f))
        assertEquals(0.75f, ReaderPrefs.nextSpeed(1.5f))
        assertTrue(ReaderPrefs.speeds.all { ReaderPrefs.nextSpeed(it) in ReaderPrefs.speeds })
    }

    @Test
    fun speedLabelHasNoLocaleSeparator() {
        assertEquals("1x", ReaderPrefs.speedLabel(1f))
        assertEquals("0.75x", ReaderPrefs.speedLabel(0.75f))
        assertEquals("1.25x", ReaderPrefs.speedLabel(1.25f))
        assertEquals("1.5x", ReaderPrefs.speedLabel(1.5f))
    }
}
