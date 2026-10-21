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

    fun surahs(): List<Surah> = surahs

    fun surah(id: Int): Surah? = surahs.firstOrNull { it.id == id }

    fun versesOf(surahId: Int): List<Verse> = verses.filter { it.surah == surahId }

    fun searchVerses(query: String, limit: Int = 50): List<Verse> {
        val term = ArabicText.normalize(query.trim())
        if (term.length < 2) return emptyList()
        return verses.asSequence()
            .filter { ArabicText.normalize(it.text).contains(term, ignoreCase = true) }
            .take(limit)
            .toList()
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
