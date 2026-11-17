package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.ui.screens.more.ContactDetails
import com.farukzahra.muslimplus.ui.screens.more.ContactRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactRequestTest {
    private val details = ContactDetails(
        appName = "Muslim Plus",
        versionName = "1.4.8",
        versionCode = 18,
        android = "14",
        device = "Pixel 6",
        language = "pt-BR",
    )

    @Test
    fun jsonCarriesEveryFieldInOrder() {
        assertEquals(
            "{\"access_key\":\"key-123\"," +
                "\"subject\":\"Muslim Plus 1.4.8 · sugestão\"," +
                "\"from_name\":\"Muslim Plus\"," +
                "\"message\":\"oi\"," +
                "\"app\":\"Muslim Plus\"," +
                "\"version\":\"1.4.8 (18)\"," +
                "\"android\":\"14\"," +
                "\"device\":\"Pixel 6\"," +
                "\"language\":\"pt-BR\"," +
                "\"botcheck\":false}",
            ContactRequest.json(
                details = details,
                accessKey = "key-123",
                subject = "Muslim Plus 1.4.8 · sugestão",
                message = "oi",
            ),
        )
    }

    @Test
    fun jsonEscapesQuotesAndBackslashesFromTheMessage() {
        val message = "ele disse \"oi\" e C:\\temp"
        val json = ContactRequest.json(details, "k", "s", message)
        // Aspas do texto viram \" e a barra invertida vira \\, sem quebrar o JSON.
        org.junit.Assert.assertTrue(
            json,
            json.contains("\"message\":\"ele disse \\\"oi\\\" e C:\\\\temp\""),
        )
    }

    @Test
    fun escapeHandlesQuotesAndBackslash() {
        assertEquals("ele disse \\\"oi\\\"", ContactRequest.escape("ele disse \"oi\""))
        assertEquals("a\\\\b", ContactRequest.escape("a\\b"))
    }

    @Test
    fun escapeHandlesLineBreaksTabsAndControls() {
        assertEquals("linha 1\\nlinha 2", ContactRequest.escape("linha 1\nlinha 2"))
        assertEquals("a\\tb", ContactRequest.escape("a\tb"))
        assertEquals("a\\rb", ContactRequest.escape("a\rb"))
        assertEquals("\\b\\f\\u0007", ContactRequest.escape("\b\u000C\u0007"))
    }

    @Test
    fun accentsStayLiteralBecauseJsonIsUtf8() {
        assertEquals("sugestão, oração, قلب", ContactRequest.escape("sugestão, oração, قلب"))
    }

    @Test
    fun signatureUsesAppVersionAndroidAndLanguage() {
        assertEquals(
            listOf(
                "Muslim Plus 1.4.8 (18)",
                "Android 14 · Pixel 6",
                "pt-BR",
            ),
            details.signature(),
        )
    }
}
