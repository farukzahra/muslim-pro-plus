package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.Onboarding
import com.farukzahra.muslimplus.settings.StartScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regra do onboarding sem Android: a flag `onboarding_done` so abre a tela
 * quando nunca foi gravada. `SettingsRepository` le a mesma chave.
 */
class OnboardingTest {

    @Test
    fun missingFlagShowsOnboarding() {
        assertEquals(StartScreen.ONBOARDING, Onboarding.startScreen(null))
        assertTrue(Onboarding.shouldShow(null))
    }

    @Test
    fun doneFlagOpensHome() {
        assertEquals(StartScreen.HOME, Onboarding.startScreen(true))
        assertFalse(Onboarding.shouldShow(true))
    }

    @Test
    fun explicitFalseStillShowsOnboarding() {
        // Valor gravado como false (formato antigo/corrompido) nao conta como
        // concluido: o usuario ainda nao viu a tela.
        assertEquals(StartScreen.ONBOARDING, Onboarding.startScreen(false))
    }

    @Test
    fun flagKeyIsTheDocumentedPreferenceName() {
        assertEquals("onboarding_done", Onboarding.DONE_KEY)
    }
}
