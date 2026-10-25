package com.farukzahra.muslimplus

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.ui.screens.onboarding.OnboardingScreen
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regressao do Galaxy: no Android 15+ o app desenha atras da barra de
 * navegacao. Com um inset grande injetado, "Comecar" precisa terminar acima
 * da area reservada, e nao embaixo dos botoes do sistema.
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun bottomActionsStayAboveSystemBars() {
        val insetPx = 600
        compose.setContent {
            MuslimPlusTheme {
                OnboardingScreen(onDone = {}, contentInsets = WindowInsets(0, 0, 0, insetPx))
            }
        }

        val root = compose.onRoot().getUnclippedBoundsInRoot()
        val start = compose.onNodeWithTag("onboarding_start")
        start.performScrollTo()
        start.assertIsDisplayed()
        val bounds = start.getUnclippedBoundsInRoot()
        val insetDp = with(compose.density) { insetPx.toDp() }

        assertTrue(
            "Comecar deveria ficar acima da barra do sistema " +
                "(bottom=${bounds.bottom}, root=${root.bottom}, inset=$insetDp)",
            bounds.bottom <= root.bottom - insetDp,
        )
    }
}
