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
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.farukzahra.muslimplus.ui.screens.more.ContactDialog
import com.farukzahra.muslimplus.ui.screens.more.ContactMessage
import com.farukzahra.muslimplus.ui.screens.more.ContactSender
import com.farukzahra.muslimplus.ui.screens.more.ContactSentDialog
import com.farukzahra.muslimplus.ui.screens.more.contactEmailIntent
import com.farukzahra.muslimplus.ui.screens.settings.PrivacyScreen
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Caminho real: grade do Mais -> cartao Fale conosco -> dialogo.
 *
 * O envio nao e clicado aqui porque bate na API de verdade. Os estados de envio
 * sao testados em [ContactDialogIsolatedTest], com sender fake, e o fallback de
 * e-mail em [ContactIntentTest].
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

/** Dialogo isolado, com rede e e-mail trocados por fakes. */
@RunWith(AndroidJUnit4::class)
class ContactDialogIsolatedTest {
    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(id: Int, vararg args: Any): String = context.getString(id, *args)

    @Test
    fun sendingDisablesTheButtonAndSuccessClosesWithConfirmation() {
        var shown by mutableStateOf(true)
        var sent by mutableStateOf(false)
        val gate = CompletableDeferred<Boolean>()
        var payload: String? = null
        val sender = ContactSender {
            payload = it
            gate.await()
        }

        compose.setContent {
            MuslimPlusTheme {
                if (shown) {
                    ContactDialog(
                        onDismiss = { shown = false },
                        onSent = {
                            shown = false
                            sent = true
                        },
                        sender = sender,
                    )
                }
                if (sent) ContactSentDialog(onDismiss = { sent = false })
            }
        }

        compose.onNodeWithTag("contact_field").performTextInput("sugestão para o app")
        compose.onNodeWithTag("contact_send").performClick()

        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_send")
            .assertIsNotEnabled()
            .assertTextEquals(text(R.string.contact_sending))

        gate.complete(true)
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("contact_dialog").fetchSemanticsNodes().isEmpty()
        }

        compose.onNodeWithTag("contact_dialog").assertDoesNotExist()
        compose.onNodeWithTag("contact_sent_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_sent_ok").performClick()
        compose.onNodeWithTag("contact_sent_dialog").assertDoesNotExist()

        val body = payload.orEmpty()
        assertTrue("payload sem a mensagem: $body", body.contains("\"message\":\"sugestão para o app"))
        assertTrue("payload sem botcheck: $body", body.contains("\"botcheck\":false"))
        assertTrue(
            "payload sem versao: $body",
            body.contains("\"version\":\"${BuildConfig.VERSION_NAME} ("),
        )
    }

    @Test
    fun errorKeepsTheTextAndOffersRetryAndEmail() {
        var shown by mutableStateOf(true)
        val sends = AtomicInteger(0)
        val emails = AtomicInteger(0)
        val sender = ContactSender {
            sends.incrementAndGet()
            false
        }

        compose.setContent {
            MuslimPlusTheme {
                if (shown) {
                    ContactDialog(
                        onDismiss = { shown = false },
                        onSent = { shown = false },
                        sender = sender,
                        sendEmail = {
                            emails.incrementAndGet()
                            false
                        },
                    )
                }
            }
        }

        compose.onNodeWithTag("contact_field").performTextInput("minha sugestão")
        compose.onNodeWithTag("contact_send").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("contact_send_failed").fetchSemanticsNodes().isNotEmpty()
        }

        assertEquals(1, sends.get())
        compose.onNodeWithTag("contact_send_failed").assertIsDisplayed()
        compose.onNodeWithTag("contact_field").assertTextContains("minha sugestão")
        compose.onNodeWithTag("contact_send")
            .assertIsEnabled()
            .assertTextEquals(text(R.string.contact_retry))

        // Sem app de e-mail, o endereco e o botao de copiar aparecem.
        compose.onNodeWithTag("contact_use_email").performClick()
        assertEquals(1, emails.get())
        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
        compose.onNodeWithTag("contact_failed").assertIsDisplayed()
        compose.onNodeWithTag("contact_email").assertTextEquals(AppLinks.CONTACT_EMAIL)
        compose.onNodeWithTag("contact_copy_email").assertExists().performClick()

        // Tentar de novo volta para a API, sem perder o texto.
        compose.onNodeWithTag("contact_send").performClick()
        compose.waitUntil(timeoutMillis = 5_000) { sends.get() == 2 }
        compose.onNodeWithTag("contact_dialog").assertIsDisplayed()
    }

    @Test
    fun emailFallbackThatWorksClosesTheDialog() {
        var shown by mutableStateOf(true)
        compose.setContent {
            MuslimPlusTheme {
                if (shown) {
                    ContactDialog(
                        onDismiss = { shown = false },
                        onSent = { shown = false },
                        sender = ContactSender { false },
                        sendEmail = { true },
                    )
                }
            }
        }

        compose.onNodeWithTag("contact_field").performTextInput("manda por e-mail")
        compose.onNodeWithTag("contact_send").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("contact_use_email").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("contact_use_email").performClick()
        compose.onNodeWithTag("contact_dialog").assertDoesNotExist()
    }
}

/** O resumo de privacidade precisa citar o Fale conosco depois do envio direto. */
@RunWith(AndroidJUnit4::class)
class PrivacyScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun summaryMentionsTheContactForm() {
        compose.setContent {
            MuslimPlusTheme { PrivacyScreen(onBack = {}) }
        }

        compose.onNodeWithTag("privacy_contact").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Web3Forms", substring = true).assertExists()
    }
}

/** O fallback por e-mail precisa sair como e-mail puro, com corpo codificado. */
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
