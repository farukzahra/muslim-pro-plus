package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.SearchResults
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultsTest {
    private val surahs = listOf(
        Surah(id = 1, nameArabic = "الفاتحة", nameLatin = "Al-Fatihah", verseCount = 7),
        Surah(id = 2, nameArabic = "البقرة", nameLatin = "Al-Baqarah", verseCount = 286),
        Surah(id = 36, nameArabic = "يس", nameLatin = "Ya-Sin", verseCount = 83),
    )

    /** Mais versos que o limite da secao, para provar o corte. */
    private val verses = (1..40).map { ayah ->
        Verse(surah = 2, ayah = ayah, text = "آية رقم $ayah")
    }

    @Test
    fun emptyQueryReturnsWholeCatalog() {
        val result = SearchResults.split("", surahs, verses)
        assertEquals(surahs, result.surahMatches)
        assertTrue(result.verseMatches.isEmpty())
        assertFalse(result.searchesVerses)
        assertTrue(result.isEmptyQuery)
    }

    @Test
    fun blankQueryIsTreatedAsEmpty() {
        val result = SearchResults.split("   ", surahs, verses)
        assertEquals(surahs, result.surahMatches)
        assertTrue(result.verseMatches.isEmpty())
    }

    @Test
    fun oneCharacterFiltersSurahsButNeverSearchesVerses() {
        val result = SearchResults.split("a", surahs, verses)
        assertTrue(result.surahMatches.isNotEmpty())
        assertTrue(result.verseMatches.isEmpty())
        assertFalse(result.searchesVerses)
    }

    @Test
    fun oneCharacterStillReachesNumericSurah() {
        val result = SearchResults.split("2", surahs, verses)
        assertEquals(listOf(2), result.surahMatches.map { it.id })
        assertTrue(result.verseMatches.isEmpty())
    }

    @Test
    fun twoCharactersSearchVerses() {
        val result = SearchResults.split("آي", surahs, verses)
        assertTrue(result.searchesVerses)
        assertTrue(result.verseMatches.isNotEmpty())
    }

    @Test
    fun verseResultsAreLimitedToThirty() {
        val result = SearchResults.split("آية", surahs, verses)
        assertEquals(SearchResults.MAX_VERSES, result.verseMatches.size)
        assertEquals(30, result.verseMatches.size)
        assertEquals(1, result.verseMatches.first().ayah)
    }

    @Test
    fun surahMatchesByNameAndNumber() {
        assertEquals(
            listOf(2),
            SearchResults.split("baqarah", surahs, emptyList()).surahMatches.map { it.id },
        )
        assertEquals(
            listOf(36),
            SearchResults.split("36", surahs, emptyList()).surahMatches.map { it.id },
        )
        assertTrue(SearchResults.split("zzz", surahs, emptyList()).surahMatches.isEmpty())
    }

    @Test
    fun arabicQueryIgnoresHarakat() {
        // "الفاتحة" com harakat nao existe no catalogo; a normalizacao resolve.
        val result = SearchResults.split("الفَاتِحَة", surahs, emptyList())
        assertEquals(listOf(1), result.surahMatches.map { it.id })
    }
}
