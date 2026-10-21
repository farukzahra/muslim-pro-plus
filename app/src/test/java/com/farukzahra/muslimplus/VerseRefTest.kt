package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.VerseRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerseRefTest {
    @Test
    fun keyRoundTrips() {
        val ref = VerseRef(2, 255)
        assertEquals("2:255", ref.toKey())
        assertEquals(ref, VerseRef.parse(ref.toKey()))
    }

    @Test
    fun parsesExtremes() {
        assertEquals(VerseRef(1, 1), VerseRef.parse("1:1"))
        assertEquals(VerseRef(114, 6), VerseRef.parse("114:6"))
        assertEquals(VerseRef(2, 286), VerseRef.parse("2:286"))
    }

    @Test
    fun parsesTrimmedInput() {
        assertEquals(VerseRef(36, 12), VerseRef.parse(" 36:12 "))
    }

    @Test
    fun rejectsMalformedKeys() {
        listOf("", " ", "abc", "2", "2:", ":255", "2:255:3", "2-255", "2:2a", "٤:٥")
            .forEach { raw -> assertNull("aceitou \"$raw\"", VerseRef.parse(raw)) }
    }

    @Test
    fun rejectsOutOfRangeValues() {
        listOf("0:1", "-1:1", "115:1", "1:0", "1:-1", "1:287", "999:999")
            .forEach { raw -> assertNull("aceitou \"$raw\"", VerseRef.parse(raw)) }
    }

    @Test
    fun rejectsNull() {
        assertNull(VerseRef.parse(null))
    }

    @Test
    fun ofValidatesRanges() {
        assertEquals(VerseRef(1, 1), VerseRef.of(1, 1))
        assertEquals(VerseRef(114, 6), VerseRef.of(114, 6))
        assertNull(VerseRef.of(0, 1))
        assertNull(VerseRef.of(115, 1))
        assertNull(VerseRef.of(1, 0))
        assertNull(VerseRef.of(1, 287))
        assertNull(VerseRef.of(-2, -255))
    }

    @Test
    fun isValidMatchesRanges() {
        assertTrue(VerseRef(2, 286).isValid)
        assertFalse(VerseRef(2, 287).isValid)
        assertFalse(VerseRef(115, 1).isValid)
        assertFalse(VerseRef(1, 0).isValid)
    }

    @Test
    fun encodedSetDropsInvalidKeys() {
        val encoded = VerseRef.encodeAll(
            listOf(VerseRef(2, 255), VerseRef(999, 1), VerseRef(1, 1)),
        )
        assertEquals(setOf("2:255", "1:1"), encoded)
    }

    @Test
    fun decodedSetIgnoresInvalidKeys() {
        val decoded = VerseRef.decodeAll(setOf("2:255", "lixo", "999:1", "1:1", "0:0"))
        assertEquals(setOf(VerseRef(2, 255), VerseRef(1, 1)), decoded)
    }

    @Test
    fun decodeAllHandlesNull() {
        assertTrue(VerseRef.decodeAll(null).isEmpty())
    }
}
