package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef

/**
 * Monta a fila de uma sura: cada verso vira um [MediaItem] com a URL remota.
 * O download local e resolvido a cada abertura por [QuranStreamingCache], para
 * que a mesma fila funcione antes e depois de baixar (ou apagar) a sura.
 */
object QuranAudioItems {
    fun forSurah(
        context: Context,
        surah: Surah,
        verses: List<Verse>,
        source: QuranAudioSource = EveryAyahSource,
    ): List<MediaItem> {
        val reciter = source.reciter
        return verses.map { verse ->
            val title = context.getString(
                R.string.quran_audio_now_playing,
                surah.nameLatin,
                verse.ayah,
            )
            MediaItem.Builder()
                .setMediaId(VerseRef(verse.surah, verse.ayah).toKey())
                .setUri(source.urlFor(surah.id, verse.ayah).toUri())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(reciter)
                        .setExtras(Bundle().apply { putString("reciter_id", source.id) })
                        .build(),
                )
                .build()
        }
    }
}
