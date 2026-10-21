package com.farukzahra.muslimplus

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranDataTest {
    private val assets = File("src/main/assets")

    private fun lines(name: String): List<String> =
        File(assets, name).readLines().filter { it.isNotBlank() }

    @Test
    fun hasAllSurahs() {
        assertEquals(114, lines("quran_surahs.txt").size)
    }

    @Test
    fun hasAllVerses() {
        assertEquals(6236, lines("quran_verses.txt").size)
    }

    @Test
    fun firstAndLastVerseAreCorrect() {
        val verses = lines("quran_verses.txt")
        assertTrue(verses.first().startsWith("1:1|"))
        assertTrue(verses.first().contains("بِسْمِ"))
        assertTrue(verses.last().startsWith("114:6|"))
    }

    @Test
    fun everySurahDeclaresItsVerseCount() {
        val surahs = lines("quran_surahs.txt").map { it.split('|') }
        assertTrue(surahs.all { it.size >= 4 && it[3].toInt() > 0 })
        assertEquals(7, surahs.first()[3].toInt())
        assertEquals(6, surahs.last()[3].toInt())
    }
}
