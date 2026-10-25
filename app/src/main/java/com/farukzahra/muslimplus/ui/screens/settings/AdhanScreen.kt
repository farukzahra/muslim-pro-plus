package com.farukzahra.muslimplus.ui.screens.settings

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.prayer.Prayer
import kotlinx.coroutines.launch

/**
 * Configuração do chamado: quais orações tocam, qual gravação e se o áudio
 * respeita o modo silencioso. Tudo é gravado na hora e vale no próximo alarme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val prayers by settings.adhanPrayers.collectAsState(initial = AdhanPolicy.defaultEnabled)
    val soundId by settings.adhanSoundId.collectAsState(initial = AdhanSound.default.id)
    val respectSilent by settings.adhanRespectSilent.collectAsState(initial = false)

    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingRes by remember { mutableStateOf<Int?>(null) }

    fun stopPreview() {
        player?.release()
        player = null
        playingRes = null
    }

    fun togglePreview(sound: AdhanSound) {
        if (playingRes == sound.resId) {
            stopPreview()
            return
        }
        stopPreview()
        player = MediaPlayer.create(context, sound.resId)?.apply {
            setOnCompletionListener { stopPreview() }
            start()
        }
        playingRes = sound.resId
    }

    DisposableEffect(Unit) {
        onDispose { player?.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.adhan_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.adhan_prayers_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp),
            )
            AdhanPolicy.eligible.forEach { prayer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(prayerLabel(prayer)),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = prayer in prayers,
                        onCheckedChange = { enabled ->
                            val next = if (enabled) prayers + prayer else prayers - prayer
                            scope.launch { settings.setAdhanPrayers(next) }
                        },
                        modifier = Modifier.testTag("adhan_${prayer.name.lowercase()}"),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.adhan_sound_title),
                style = MaterialTheme.typography.titleMedium,
            )
            AdhanSound.entries.forEach { sound ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = sound.id == soundId,
                        onClick = { scope.launch { settings.setAdhanSound(sound.id) } },
                        modifier = Modifier.testTag("adhan_sound_${sound.id}"),
                    )
                    Text(
                        text = stringResource(sound.labelRes),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                    TextButton(
                        onClick = { togglePreview(sound) },
                        modifier = Modifier.testTag("adhan_preview_${sound.id}"),
                    ) {
                        Text(
                            stringResource(
                                if (playingRes == sound.resId) R.string.adhan_stop else R.string.adhan_preview,
                            ),
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.adhan_volume_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.adhan_respect_silent),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.adhan_respect_silent_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Switch(
                    checked = respectSilent,
                    onCheckedChange = { scope.launch { settings.setAdhanRespectSilent(it) } },
                    modifier = Modifier.testTag("adhan_respect_silent"),
                )
            }
        }
    }
}

private fun prayerLabel(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
