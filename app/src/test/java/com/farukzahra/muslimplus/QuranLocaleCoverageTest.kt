package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.AppLocale
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guarda a regra: toda lingua suportada pelo app precisa de traducao do
 * Alcorao. Se alguem adicionar um locale em AppLocale.supported sem gerar o
 * asset correspondente, este teste quebra o build.
 */
class QuranLocaleCoverageTest {
    private val assets = File("src/main/assets")

    @Test
    fun everySupportedLocaleHasATranslation() {
        val translated = AppLocale.supported.filter { it != "ar" }
        assertTrue("nenhum locale traduzido", translated.isNotEmpty())
        translated.forEach { tag ->
            val file = File(assets, "quran_translation_$tag.txt")
            assertTrue("falta quran_translation_$tag.txt para o locale $tag", file.exists())
            assertEquals(
                "traducao de $tag incompleta",
                6236,
                file.readLines().count { it.isNotBlank() },
            )
        }
    }

    @Test
    fun arabicComesFromTheUthmaniText() {
        val file = File(assets, "quran_verses.txt")
        assertTrue(file.exists())
        assertEquals(6236, file.readLines().count { it.isNotBlank() })
    }

    @Test
    fun transliterationIsComplete() {
        val file = File(assets, "quran_transliteration.txt")
        assertTrue(file.exists())
        assertEquals(6236, file.readLines().count { it.isNotBlank() })
    }
}
