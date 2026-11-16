package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.ui.screens.more.ContactMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactMessageTest {
    @Test
    fun normalizeTrimsAndNormalizesLineBreaks() {
        assertEquals("oi\nmundo", ContactMessage.normalize("  oi\r\nmundo  "))
        assertEquals("a\nb", ContactMessage.normalize("a\rb"))
    }

    @Test
    fun validityNeedsThreeRealCharacters() {
        assertFalse(ContactMessage.isValid(""))
        assertFalse(ContactMessage.isValid("   "))
        assertFalse(ContactMessage.isValid("oi"))
        assertFalse(ContactMessage.isValid("  oi  "))
        assertTrue(ContactMessage.isValid("oi!"))
    }

    @Test
    fun limitCapsTheMessageAtTheMaximum() {
        val long = "a".repeat(ContactMessage.MAX_LENGTH + 50)
        assertEquals(ContactMessage.MAX_LENGTH, ContactMessage.limit(long).length)
        assertEquals("curta", ContactMessage.limit("curta"))
    }

    @Test
    fun bodyWithoutHeaderHasNoDanglingSeparator() {
        assertEquals("oi", ContactMessage.body("  oi  "))
        assertEquals("oi", ContactMessage.body("oi", listOf("", "   ")))
    }

    @Test
    fun bodyAppendsTheTechnicalDetails() {
        val body = ContactMessage.body(
            message = "oi\n",
            header = listOf("Muslim Plus 1.4.8 (18)", "Android 14 · Pixel 6", "pt-BR"),
        )
        assertEquals(
            "oi" + ContactMessage.DETAILS_SEPARATOR +
                "Muslim Plus 1.4.8 (18)\nAndroid 14 · Pixel 6\npt-BR",
            body,
        )
    }

    @Test
    fun mailtoEncodesSpaceAsPercent20NotPlus() {
        val uri = ContactMessage.mailto("a@b.c", "Oi mundo", "linha um")
        assertEquals(
            "mailto:a@b.c?subject=Oi%20mundo&body=linha%20um",
            uri,
        )
        assertFalse("espaco virou +", uri.contains("+"))
    }

    @Test
    fun mailtoEncodesAccentsAndLineBreaksInUtf8() {
        assertEquals(
            "mailto:farukz@gmail.com?subject=Muslim%20Plus%201.4.8%20%C2%B7%20sugest%C3%A3o" +
                "&body=linha%201%0Alinha%202",
            ContactMessage.mailto(
                email = " farukz@gmail.com ",
                subject = "Muslim Plus 1.4.8 · sugestão",
                body = "linha 1\nlinha 2",
            ),
        )
    }

    @Test
    fun encodeKeepsUnreservedAndEscapesTheRest() {
        assertEquals("a-b_c.d~e", ContactMessage.encode("a-b_c.d~e"))
        assertEquals("%20", ContactMessage.encode(" "))
        assertEquals("%40", ContactMessage.encode("@"))
        assertEquals("%26", ContactMessage.encode("&"))
    }
}
