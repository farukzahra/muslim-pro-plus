package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef

/**
 * Monta a fila de uma sura: cada verso vira um [MediaItem], tocando do arquivo
 * baixado quando existe e do CDN quando nao existe. O player nao sabe qual dos
 * dois foi usado.
 */
object QuranAudioItems {
    fun forSurah(
        context: Context,
        store: QuranAudioStore,
        surah: Surah,
        verses: List<Verse>,
        source: QuranAudioSource = EveryAyahSource,
    ): List<MediaItem> {
        val reciter = context.getString(R.string.quran_reciter_alafasy)
        return verses.map { verse ->
            val local = store.localAudio(surah.id, verse.ayah)
            val uri = local?.toUri() ?: source.urlFor(surah.id, verse.ayah).toUri()
            val title = context.getString(
                R.string.quran_audio_now_playing,
                surah.nameLatin,
                verse.ayah,
            )
            MediaItem.Builder()
                .setMediaId(VerseRef(verse.surah, verse.ayah).toKey())
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(reciter)
                        .build(),
                )
                .build()
        }
    }
}
