package com.farukzahra.muslimplus.adhan

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.notifications.Notifications
import com.farukzahra.muslimplus.prayer.Prayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Toca o adhan no horario da oracao. Usa o fluxo de ALARME, entao o audio sai
 * no volume de alarme mesmo com o aparelho em silencioso (a opcao de respeitar
 * o silencioso e decidida antes, no receiver). Para sozinho no fim ou no erro.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class AdhanService : Service() {
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_ALARM)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                // O foco automatico do Media3 so aceita USAGE_MEDIA/GAME; alarme
                // nao pede foco, como qualquer som de alarme do sistema.
                /* handleAudioFocus = */ false,
            )
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) stopSelf()
                if (playbackState == Player.STATE_IDLE && player.playerError != null) stopSelf()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val sound = AdhanSound.find(intent.getStringExtra(EXTRA_SOUND))
        val prayer = runCatching {
            Prayer.valueOf(intent.getStringExtra(EXTRA_PRAYER).orEmpty())
        }.getOrNull()

        try {
            startForeground(NOTIFICATION_ID, notification(prayer))
            _playing.value = true
        } catch (error: Throwable) {
            android.util.Log.e("AdhanService", "startForeground failed", error)
            stopSelf()
            return START_NOT_STICKY
        }
        player.setMediaItem(MediaItem.fromUri("android.resource://$packageName/${sound.resId}"))
        player.prepare()
        player.play()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _playing.value = false
        player.release()
        super.onDestroy()
    }

    private fun notification(prayer: Prayer?): Notification {
        ensureChannel()
        val stop = PendingIntent.getService(
            this,
            0,
            Intent(this, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = prayer?.let {
            getString(R.string.adhan_now, getString(Notifications.prayerLabel(it)))
        } ?: getString(R.string.adhan_title)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.tab_prayers)
            .setContentTitle(title)
            .setContentText(getString(R.string.adhan_playing))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.tab_prayers, getString(R.string.adhan_stop), stop)
            .build()
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.adhan_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        channel.setSound(null, null)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "adhan_playback"
        internal const val NOTIFICATION_ID = 2019
        private const val ACTION_STOP = "com.farukzahra.muslimplus.adhan.STOP"
        private const val EXTRA_PRAYER = "prayer"
        private const val EXTRA_SOUND = "sound"

        private val _playing = MutableStateFlow(false)

        /** Estado observável pelo app para mostrar o botão de parar. */
        val playing: StateFlow<Boolean> = _playing.asStateFlow()

        fun start(context: Context, prayer: Prayer, sound: AdhanSound) {
            val intent = Intent(context, AdhanService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_SOUND, sound.id)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                // Alarme inexato/sem permissao de primeiro plano: a notificacao
                // continua valendo e o app nao pode cair por causa do audio.
            }
        }

        /** Para o chamado em andamento; tambem e a acao da notificacao. */
        fun stop(context: Context) {
            val intent = Intent(context, AdhanService::class.java).setAction(ACTION_STOP)
            try {
                context.startService(intent)
            } catch (_: Exception) {
                context.stopService(Intent(context, AdhanService::class.java))
            }
        }
    }
}
