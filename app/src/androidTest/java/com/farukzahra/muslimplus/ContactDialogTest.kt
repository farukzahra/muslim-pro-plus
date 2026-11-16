package com.farukzahra.muslimplus

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.ui.screens.more.ContactDialog
import com.farukzahra.muslimplus.ui.screens.more.ContactMessage
import com.farukzahra.muslimplus.ui.screens.more.contactEmailIntent
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Caminho real: grade do Mais -> cartao Fale conosco -> dialogo.
 *
 * O envio nao e clicado aqui porque abriria o app de e-mail do aparelho e
 * tiraria o app do primeiro plano. O caminho de envio e testado em
 * [ContactDialogIsolatedTest], com callback injetado, e o intent em
 * [ContactIntentTest].
 */
@RunWith(AndroidJUnit4::class)
class ContactDialogTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun moreCardOpensDialogTypingEnablesSendAndCancelCloses() {
        runBlocking { ServiceLocator.settings.setOnboardingDone() }
        compose.activityRule.scenario.recreate()
        val context = compose.activity

        compose.onNodeWithText(context.getString(R.string.tab_more)).performClick()
        compose.onNodeWithTag("more_contact").performScrollTo().performClick()

        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_counter", useUnmergedTree = true).assertTextEquals(
            context.getString(R.string.contact_counter, 0, ContactMessage.MAX_LENGTH),
        )
        compose.onNodeWithTag("contact_send").assertIsNotEnabled()

        compose.onNodeWithTag("contact_field").performTextInput("quero um gráfico semanal")
        compose.onNodeWithTag("contact_send").assertIsEnabled()
        compose.onNodeWithTag("contact_counter", useUnmergedTree = true).assertTextEquals(
            context.getString(R.string.contact_counter, 24, ContactMessage.MAX_LENGTH),
        )

        compose.onNodeWithTag("contact_cancel").performClick()
        compose.onNodeWithTag("contact_dialog").assertDoesNotExist()
    }
}

/** Dialogo isolado, com o envio trocado por callback: cobre sucesso e fallback. */
@RunWith(AndroidJUnit4::class)
class ContactDialogIsolatedTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun sendFailureKeepsTheTextAndOffersTheEmailToCopy() {
        var shown by mutableStateOf(true)
        compose.setContent {
            MuslimPlusTheme {
                if (shown) {
                    ContactDialog(onDismiss = { shown = false }, onSend = { false })
                }
            }
        }

        compose.onNodeWithTag("contact_field").performTextInput("minha sugestão")
        compose.onNodeWithTag("contact_send").performClick()

        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_failed").assertIsDisplayed()
        compose.onNodeWithTag("contact_email").assertTextEquals(AppLinks.CONTACT_EMAIL)
        compose.onNodeWithTag("contact_copy_email").assertExists().performClick()
        // Copiar nao pode fechar o dialogo nem perder o texto ja escrito.
        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_field").assertTextContains("minha sugestão")
    }

    @Test
    fun sendSuccessPassesTheWrittenTextAndCloses() {
        var shown by mutableStateOf(true)
        var received: String? = null
        compose.setContent {
            MuslimPlusTheme {
                if (shown) {
                    ContactDialog(
                        onDismiss = { shown = false },
                        onSend = {
                            received = it
                            true
                        },
                    )
                }
            }
        }

        compose.onNodeWithTag("contact_field").performTextInput("sugestão enviada")
        compose.onNodeWithTag("contact_send").performClick()

        assertEquals("sugestão enviada", received)
        compose.onNodeWithTag("contact_dialog").assertDoesNotExist()
    }
}

/** O intent precisa sair como e-mail puro, com assunto e corpo ja codificados. */
@RunWith(AndroidJUnit4::class)
class ContactIntentTest {
    @Test
    fun intentIsSendToMailtoWithEncodedBody() {
        val intent = contactEmailIntent(
            subject = "Muslim Plus 1.4.8 · sugestão",
            body = "oi\nfim",
        )

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto", intent.data?.scheme)
        val data = intent.data.toString()
        assertTrue("destinatario errado: $data", data.startsWith("mailto:${AppLinks.CONTACT_EMAIL}?subject="))
        assertTrue("assunto nao codificado: $data", data.contains("%C2%B7%20sugest%C3%A3o"))
        assertTrue("corpo nao codificado: $data", data.contains("body=oi%0Afim"))
    }
}
