package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.QuranModes
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranModesTest {
    @Test
    fun portugueseDefaultsToArabicPlusPortuguese() {
        assertEquals(setOf("ar", "pt-BR"), QuranModes.defaults("pt-BR"))
        assertEquals(setOf("ar", "pt-BR"), QuranModes.defaults("pt_BR"))
        assertEquals(setOf("ar", "pt-BR"), QuranModes.defaults("pt"))
    }

    @Test
    fun englishDefaultsToArabicPlusEnglish() {
        assertEquals(setOf("ar", "en"), QuranModes.defaults("en-US"))
    }

    @Test
    fun arabicUsesOnlyTheOriginalText() {
        assertEquals(setOf("ar"), QuranModes.defaults("ar-EG"))
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglish() {
        assertEquals(setOf("ar", "en"), QuranModes.defaults("fr-FR"))
        assertEquals(setOf("ar", "en"), QuranModes.defaults(null))
    }
}
