package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.DailyVerse
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A lista curada e' embarcada no codigo (nao no asset), mas cada par precisa
 * existir no `quran_verses.txt`; este teste e' o gate contra um par errado.
 */
class DailyVerseTest {
    private val assets = File("src/main/assets")

    private fun verses(): Map<String, String> =
        File(assets, "quran_verses.txt").readLines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val separator = line.indexOf('|')
                if (separator <= 0) {
                    null
                } else {
                    line.substring(0, separator) to line.substring(separator + 1)
                }
            }
            .toMap()

    @Test
    fun curatedListHasOneHundredPairs() {
        assertEquals(100, DailyVerse.keys.size)
    }

    @Test
    fun noPairIsRepeatedAndSurahsAreInRange() {
        val pairs = DailyVerse.keys.map { it.surah to it.ayah }
        assertEquals(pairs.size, pairs.toSet().size)
        DailyVerse.keys.forEach { key ->
            assertTrue("sura fora de 1..114: $key", key.surah in 1..114)
            assertTrue("ayah invalido: $key", key.ayah >= 1)
        }
    }

    @Test
    fun everyPairExistsInTheAssetWithText() {
        val verses = verses()
        DailyVerse.keys.forEach { key ->
            val text = verses["${key.surah}:${key.ayah}"]
            assertTrue("par ausente no asset: ${key.surah}:${key.ayah}", text != null)
            assertTrue("versiculo vazio: ${key.surah}:${key.ayah}", !text.isNullOrBlank())
        }
    }

    @Test
    fun sameDateAlwaysReturnsTheSameVerse() {
        listOf(
            LocalDate.of(2026, 10, 21),
            LocalDate.of(2027, 3, 1),
            LocalDate.of(1999, 12, 31),
        ).forEach { date ->
            assertEquals(DailyVerse.keyFor(date), DailyVerse.keyFor(date))
            assertEquals(DailyVerse.indexFor(date), DailyVerse.indexFor(date))
        }
    }

    @Test
    fun consecutiveDatesAdvanceOneIndexAndWrapAround() {
        val start = LocalDate.of(2026, 10, 21)
        val size = DailyVerse.keys.size
        val first = DailyVerse.indexFor(start)
        for (day in 1..size * 2L) {
            val expected = Math.floorMod(first + day, size.toLong()).toInt()
            assertEquals(
                "dia +$day",
                expected,
                DailyVerse.indexFor(start.plusDays(day)),
            )
        }
    }

    @Test
    fun theWholeCycleVisitsEveryPairOnce() {
        val start = LocalDate.of(2026, 1, 1)
        val size = DailyVerse.keys.size
        val seen = (0 until size).map { DailyVerse.keyFor(start.plusDays(it.toLong())) }
        assertEquals(size, seen.toSet().size)
    }
}
