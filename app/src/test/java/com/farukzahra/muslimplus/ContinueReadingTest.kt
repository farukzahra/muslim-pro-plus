package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.ContinueReading
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContinueReadingTest {
    private val surahs = listOf(
        Surah(id = 1, nameArabic = "الفاتحة", nameLatin = "Al-Fatihah", verseCount = 7),
        Surah(id = 2, nameArabic = "البقرة", nameLatin = "Al-Baqarah", verseCount = 286),
        Surah(id = 114, nameArabic = "الناس", nameLatin = "An-Nas", verseCount = 6),
    )

    @Test
    fun noCardWithoutLastRead() {
        assertNull(ContinueReading.resolve(null, surahs))
    }

    @Test
    fun keepsValidLastRead() {
        assertEquals(VerseRef(2, 255), ContinueReading.resolve(VerseRef(2, 255), surahs))
    }

    @Test
    fun dropsSurahMissingFromCatalog() {
        assertNull(ContinueReading.resolve(VerseRef(50, 1), surahs))
    }

    @Test
    fun dropsAyahBeyondSurahVerseCount() {
        // Valido no codec global (1..286), inexistente em Al-Fatihah (7 versos).
        assertNull(ContinueReading.resolve(VerseRef(1, 8), surahs))
    }

    @Test
    fun emptyCatalogHidesCard() {
        assertNull(ContinueReading.resolve(VerseRef(2, 255), emptyList()))
    }

    @Test
    fun startAyahOnlyForSameSurah() {
        assertEquals(255, ContinueReading.startAyah(VerseRef(2, 255), surahs[1]))
        assertNull(ContinueReading.startAyah(VerseRef(2, 255), surahs[0]))
        assertNull(ContinueReading.startAyah(null, surahs[1]))
    }

    @Test
    fun startAyahRejectsImpossibleVerse() {
        assertNull(ContinueReading.startAyah(VerseRef(1, 8), surahs[0]))
    }

    @Test
    fun verseIndexMapsLoadedList() {
        val verses = listOf(
            Verse(surah = 1, ayah = 1, text = "بِسْمِ"),
            Verse(surah = 1, ayah = 2, text = "الْحَمْدُ"),
            Verse(surah = 1, ayah = 3, text = "الرَّحْمَٰنِ"),
        )
        assertEquals(2, ContinueReading.verseIndex(verses, 3))
        assertNull(ContinueReading.verseIndex(verses, 7))
        assertNull(ContinueReading.verseIndex(emptyList(), 1))
    }
}
