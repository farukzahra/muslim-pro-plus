package com.farukzahra.muslimplus.quran

import android.content.Context

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameLatin: String,
    val verseCount: Int,
)

data class Verse(
    val surah: Int,
    val ayah: Int,
    val text: String,
)

/** Normalizacao arabica para busca: sem harakat, alef/ya unificados. */
object ArabicText {
    fun normalize(text: String): String {
        val out = StringBuilder(text.length)
        for (ch in text) {
            when (ch) {
                in '\u064B'..'\u065F', '\u0670', in '\u06D6'..'\u06ED' -> Unit
                'أ', 'إ', 'آ', 'ٱ' -> out.append('ا')
                'ى' -> out.append('ي')
                'ؤ' -> out.append('و')
                'ئ' -> out.append('ي')
                'ة' -> out.append('ه')
                else -> out.append(ch)
            }
        }
        return out.toString()
    }
}

class QuranRepository(private val context: Context) {
    private val surahs: List<Surah> by lazy { loadSurahs() }
    private val verses: List<Verse> by lazy { loadVerses() }
    private val singleVerses = mutableMapOf<String, Verse>()
    private val caches = mutableMapOf<String, Map<String, String>>()

    /**
     * Texto dos versos ja normalizado, montado uma vez: a busca por tecla nao
     * pode normalizar 6.236 versos a cada consulta.
     */
    private val searchIndex: List<Pair<Verse, String>> by lazy {
        verses.map { verse -> verse to ArabicText.normalize(verse.text) }
    }

    fun surahs(): List<Surah> = surahs

    fun surah(id: Int): Surah? = surahs.firstOrNull { it.id == id }

    fun versesOf(surahId: Int): List<Verse> = verses.filter { it.surah == surahId }

    /**
     * Um verso isolado (verso do dia). Le o asset direto, sem disparar o
     * carregamento dos 6.236 versos na abertura da aba Hoje.
     */
    fun verse(surah: Int, ayah: Int): Verse? {
        val key = verseKey(surah, ayah)
        singleVerses[key]?.let { return it }
        val verse = readVerse(key) ?: return null
        singleVerses[key] = verse
        return verse
    }

    private fun verseKey(surah: Int, ayah: Int): String = "$surah:$ayah"

    private fun readVerse(key: String): Verse? =
        runCatching {
            context.assets.open("quran_verses.txt").bufferedReader().useLines { lines ->
                val line = lines.firstOrNull { it.startsWith("$key|") } ?: return@useLines null
                Verse(
                    surah = key.substringBefore(':').toInt(),
                    ayah = key.substringAfter(':').toInt(),
                    text = line.substringAfter('|'),
                )
            }
        }.getOrNull()

    /** Carrega (uma vez) a camada pedida: "translit" ou um locale como "en". */
    fun preload(mode: String) {
        caches.getOrPut(mode) {
            val asset = when (mode) {
                "translit" -> "quran_transliteration.txt"
                else -> "quran_translation_$mode.txt"
            }
            runCatching { loadMap(asset) }.getOrDefault(emptyMap())
        }
    }

    fun layer(mode: String, verseKey: String): String? {
        preload(mode)
        return caches[mode]?.get(verseKey)
    }

    fun searchVerses(query: String, limit: Int = 50): List<Verse> {
        val term = ArabicText.normalize(query.trim())
        if (term.length < 2) return emptyList()
        return searchIndex.asSequence()
            .filter { (_, text) -> text.contains(term, ignoreCase = true) }
            .map { (verse, _) -> verse }
            .take(limit)
            .toList()
    }

    private fun loadMap(asset: String): Map<String, String> =
        context.assets.open(asset).bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val separator = line.indexOf('|')
                if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
            }.toMap()
        }

    private fun loadSurahs(): List<Surah> =
        context.assets.open("quran_surahs.txt").bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val parts = line.split('|')
                if (parts.size < 4) return@mapNotNull null
                Surah(
                    id = parts[0].toIntOrNull() ?: return@mapNotNull null,
                    nameArabic = parts[1],
                    nameLatin = parts[2],
                    verseCount = parts[3].toIntOrNull() ?: 0,
                )
            }.toList()
        }

    private fun loadVerses(): List<Verse> =
        context.assets.open("quran_verses.txt").bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val separator = line.indexOf('|')
                if (separator <= 0) return@mapNotNull null
                val key = line.substring(0, separator).split(':')
                if (key.size != 2) return@mapNotNull null
                Verse(
                    surah = key[0].toIntOrNull() ?: return@mapNotNull null,
                    ayah = key[1].toIntOrNull() ?: return@mapNotNull null,
                    text = line.substring(separator + 1),
                )
            }.toList()
        }
}
