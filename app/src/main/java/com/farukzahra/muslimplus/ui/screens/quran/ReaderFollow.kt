package com.farukzahra.muslimplus.ui.screens.quran

import com.farukzahra.muslimplus.quran.Verse

/**
 * Decide para onde a leitura deve rolar enquanto o áudio toca. Devolve `null`
 * quando não é para mexer na lista: áudio parado, outra surata ou verso que não
 * está na lista aberta.
 */
object ReaderFollow {
    fun targetIndex(
        verses: List<Verse>,
        surah: Int?,
        playingSurah: Int?,
        playingAyah: Int?,
        playing: Boolean,
    ): Int? {
        if (!playing || surah == null || playingSurah != surah) return null
        val ayah = playingAyah ?: return null
        return verses.indexOfFirst { it.ayah == ayah }.takeIf { it >= 0 }
    }
}
