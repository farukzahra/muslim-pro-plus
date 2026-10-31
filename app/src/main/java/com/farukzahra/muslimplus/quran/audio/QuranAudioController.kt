package com.farukzahra.muslimplus.quran.audio

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.farukzahra.muslimplus.quran.VerseRef
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** O que a tela precisa saber: qual verso toca, se esta tocando e a velocidade. */
data class QuranAudioState(
    val available: Boolean = false,
    val playing: Boolean = false,
    val surah: Int? = null,
    val ayah: Int? = null,
    val reciterId: String? = null,
    val failed: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val speed: Float = 1f,
)

/**
 * Ponte entre o leitor e o [QuranPlaybackService].
 *
 * A tela nunca cria um ExoPlayer: ela manda uma fila e observa [state]. Assim a
 * reproducao sobrevive a rotacao, a tela apagada e a saida do app, e o service
 * continua sendo o unico dono do player.
 */
class QuranAudioController(context: Context) {
    private val appContext = context.applicationContext
    private val executor = ContextCompat.getMainExecutor(appContext)

    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var pending: PendingQueue? = null
    private var future: ListenableFuture<MediaController>? = null
    private var released = false

    /** Velocidade desejada; aplicada em toda fila nova e restaurada ao conectar. */
    private var speed = 1f

    private data class PendingQueue(
        val items: List<MediaItem>,
        val startIndex: Int,
    )

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
    }

    /** Conecta ao service. Idempotente: chamar de novo nao abre outra sessao. */
    fun connect() {
        if (controller != null || future != null || released) return
        val token = SessionToken(
            appContext,
            ComponentName(appContext, QuranPlaybackService::class.java),
        )
        val connecting = MediaController.Builder(appContext, token).buildAsync()
        future = connecting
        connecting.addListener(
            {
                if (released) {
                    MediaController.releaseFuture(connecting)
                    return@addListener
                }
                runCatching { connecting.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        future = null
                        mediaController.addListener(listener)
                        mediaController.setPlaybackSpeed(speed)
                        publish(mediaController)
                        pending?.let { queue ->
                            start(mediaController, queue.items, queue.startIndex)
                            pending = null
                        }
                    }
                    .onFailure { _state.value = QuranAudioState(available = false) }
            },
            executor,
        )
    }

    /** Troca a fila pela sura pedida e comeca no verso [startIndex]. */
    fun play(items: List<MediaItem>, startIndex: Int) {
        val mediaController = controller
        if (mediaController == null) {
            pending = PendingQueue(items, startIndex)
            connect()
            return
        }
        start(mediaController, items, startIndex)
    }

    fun pause() {
        pending = null
        controller?.pause()
    }

    fun resume() {
        if (controller?.playbackState == Player.STATE_IDLE || controller?.playerError != null) {
            controller?.prepare()
        }
        controller?.play()
    }

    /** Próximo verso da fila da sura, continuando a tocar. */
    fun next() {
        val mediaController = controller ?: return
        if (!mediaController.hasNextMediaItem()) return
        mediaController.seekToNextMediaItem()
        mediaController.play()
    }

    fun previous() {
        val mediaController = controller ?: return
        if (!mediaController.hasPreviousMediaItem()) return
        mediaController.seekToPreviousMediaItem()
        mediaController.play()
    }

    /** Velocidade salva pelo usuário; vale para a fila atual e para as próximas. */
    fun setSpeed(value: Float) {
        speed = value
        controller?.setPlaybackSpeed(value)
        controller?.let(::publish)
    }

    fun release() {
        released = true
        pending = null
        controller?.run {
            removeListener(listener)
            release()
        }
        controller = null
        future?.let { MediaController.releaseFuture(it) }
        future = null
    }

    private fun start(mediaController: MediaController, items: List<MediaItem>, startIndex: Int) {
        if (items.isEmpty()) return
        mediaController.setMediaItems(items, startIndex.coerceIn(items.indices), 0L)
        mediaController.setPlaybackSpeed(speed)
        mediaController.prepare()
        mediaController.play()
    }

    private fun publish(player: Player) {
        val ref = VerseRef.parse(player.currentMediaItem?.mediaId)
        _state.value = QuranAudioState(
            available = true,
            playing = player.isPlaying && player.playbackState != Player.STATE_ENDED,
            surah = ref?.surah,
            ayah = ref?.ayah,
            reciterId = player.currentMediaItem?.mediaMetadata?.extras?.getString("reciter_id"),
            failed = player.playerError != null,
            hasNext = player.hasNextMediaItem(),
            hasPrevious = player.hasPreviousMediaItem(),
            speed = player.playbackParameters.speed,
        )
    }
}
