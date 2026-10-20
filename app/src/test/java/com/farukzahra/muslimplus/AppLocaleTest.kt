package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLocaleTest {
    @Test
    fun followsSystemLocaleWhenThereIsNoOverride() {
        assertEquals("pt-BR", AppLocale.resolve(null, "pt_BR"))
    }

    @Test
    fun arabicSystemLocaleIsSupported() {
        assertEquals("ar", AppLocale.resolve(null, "ar_EG"))
    }

    @Test
    fun unsupportedSystemLocaleFallsBackToEnglish() {
        assertEquals("en", AppLocale.resolve(null, "fr_FR"))
    }

    @Test
    fun explicitOverrideWinsOverSystem() {
        assertEquals("ar", AppLocale.resolve("ar", "pt_BR"))
    }

    @Test
    fun languageOnlyOverrideMatchesRegionalVariant() {
        assertEquals("pt-BR", AppLocale.resolve("pt", "en_US"))
    }

    @Test
    fun unknownOverrideFallsBackToEnglish() {
        assertEquals("en", AppLocale.resolve("xx", "en_US"))
    }

    @Test
    fun missingEverythingFallsBackToEnglish() {
        assertEquals("en", AppLocale.resolve(null, null))
    }
}
