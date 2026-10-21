package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.EveryAyahSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * O formato do CDN e o contrato da fonte: qualquer mudanca de padding quebra
 * aqui, antes de chegar no player.
 */
class QuranAudioSourceTest {
    @Test
    fun firstVerseOfAlFatihah() {
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
            EveryAyahSource.urlFor(1, 1),
        )
    }

    @Test
    fun ayahAlKursiKeepsThreeDigits() {
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            EveryAyahSource.urlFor(2, 255),
        )
    }

    @Test
    fun lastSurahOfTheQuran() {
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/114006.mp3",
            EveryAyahSource.urlFor(114, 6),
        )
    }

    @Test
    fun fileNameMatchesTheUrlTail() {
        assertEquals("114006.mp3", EveryAyahSource.fileNameFor(114, 6))
        assertEquals(
            EveryAyahSource.urlFor(36, 12),
            EveryAyahSource.BASE_URL + EveryAyahSource.fileNameFor(36, 12),
        )
    }
}
