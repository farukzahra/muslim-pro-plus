package com.farukzahra.muslimplus.quran.audio

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.farukzahra.muslimplus.MainActivity
import com.farukzahra.muslimplus.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Service do Media3: dono do ExoPlayer e da MediaSession. Tocar daqui (e nao do
 * ViewModel) e o que permite continuar com a tela apagada e com os controles
 * na notificacao, que o [MediaSessionService] publica sozinho.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class QuranPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(QuranStreamingCache.factory(this)))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        activePlayer = player
        scope.launch {
            ServiceLocator.settings.quranReciterId.collect { id ->
                val current = player.currentMediaItem ?: return@collect
                val playingVoice = current.mediaMetadata.extras?.getString("reciter_id") ?: return@collect
                if (playingVoice == id) return@collect
                val voice = QuranReciters.find(id) ?: return@collect
                // Pausa e remonta a mesma fila na voz nova; o verso atual continua selecionado,
                // mas nada toca até o usuário mandar. A notificação não fica com a voz antiga.
                player.pause()
                val index = player.currentMediaItemIndex
                val items = (0 until player.mediaItemCount).mapNotNull { position ->
                    val item = player.getMediaItemAt(position)
                    val parts = item.mediaId.split(":")
                    val surah = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                    val ayah = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                    item.buildUpon()
                        .setUri(voice.urlFor(surah, ayah))
                        .setMediaMetadata(
                            item.mediaMetadata.buildUpon()
                                .setArtist(voice.reciter)
                                .setExtras(Bundle().apply { putString("reciter_id", voice.id) })
                                .build(),
                        )
                        .build()
                }
                if (items.isEmpty()) {
                    player.clearMediaItems()
                } else {
                    player.setMediaItems(items, index.coerceIn(0, items.lastIndex), 0)
                }
            }
        }
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Sem nada tocando, o app pode ser removido da recentes sem deixar o service vivo. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        activePlayer = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        private var activePlayer: ExoPlayer? = null

        /** Chamado na main thread antes de limpar arquivos de cache/downloads. */
        fun pauseForMaintenance() { activePlayer?.stop() }
    }
}
