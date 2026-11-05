package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.QuranReciters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Catálogo de recitadores: ids, pastas do CDN e bandeiras. */
class QuranReciterCatalogTest {
    @Test
    fun catalogHasEnoughVoices() {
        assertTrue("recitadores: ${QuranReciters.all.size}", QuranReciters.all.size >= 20)
    }

    @Test
    fun idsAndFoldersAreUnique() {
        assertEquals(QuranReciters.all.size, QuranReciters.all.map { it.id }.toSet().size)
        assertEquals(QuranReciters.all.size, QuranReciters.all.map { it.folder }.toSet().size)
    }

    @Test
    fun everyVoiceHasACountryFlag() {
        assertTrue(QuranReciters.all.all { it.country != null })
    }

    @Test
    fun urlUsesTheVoiceFolder() {
        assertEquals(
            "https://everyayah.com/data/Hudhaify_128kbps/001001.mp3",
            QuranReciters.find("hudhaify-128")!!.urlFor(1, 1),
        )
        assertEquals(
            "https://everyayah.com/data/Parhizgar_48kbps/114006.mp3",
            QuranReciters.find("parhizgar-48")!!.urlFor(114, 6),
        )
    }

    @Test
    fun defaultVoiceStillIsAlafasy() {
        assertEquals("alafasy-128", QuranReciters.default.id)
    }
}
