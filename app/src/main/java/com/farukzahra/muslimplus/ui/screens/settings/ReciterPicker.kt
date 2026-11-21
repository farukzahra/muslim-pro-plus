package com.farukzahra.muslimplus.ui.screens.settings

import android.content.Context
import android.media.MediaPlayer
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.audio.QuranReciter
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import com.farukzahra.muslimplus.ui.components.CountryFlag

/**
 * Lista de vozes em uma folha, com qualidade e espaço já baixado. É a mesma
 * folha usada no leitor e na tela de áudio, para a troca de recitador ser um
 * toque em qualquer lugar.
 *
 * Cada voz traz uma amostra curta embarcada (Al-Fátiha, 1º verso): o botão
 * Ouvir toca o trecho antes de a pessoa escolher, sem depender de internet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterPickerSheet(
    selectedId: String,
    sizes: Map<String, Long>,
    onPick: (QuranReciter) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }

    fun stopPreview() {
        player?.release()
        player = null
        playingId = null
    }

    fun togglePreview(voice: QuranReciter) {
        if (playingId == voice.id) {
            stopPreview()
            return
        }
        stopPreview()
        player = MediaPlayer.create(context, voice.previewRes)?.apply {
            setOnCompletionListener { stopPreview() }
            start()
        }
        playingId = voice.id
    }

    DisposableEffect(Unit) {
        onDispose { player?.release() }
    }

    ModalBottomSheet(
        onDismissRequest = {
            stopPreview()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.audio_choose_voice),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
            )
            LazyColumn(modifier = Modifier.heightIn(max = 460.dp)) {
                items(QuranReciters.all, key = { it.id }) { voice ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = voice.reciter,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    R.string.audio_voice_quality,
                                    voice.bitrateKbps,
                                    formatBytes(context, sizes[voice.id] ?: 0L),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = voice.id == selectedId, onClick = null)
                                CountryFlag(
                                    country = voice.country,
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                                )
                            }
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { togglePreview(voice) },
                                modifier = Modifier.testTag("reciter_preview_${voice.id}"),
                            ) {
                                Text(
                                    stringResource(
                                        if (playingId == voice.id) R.string.adhan_stop else R.string.adhan_preview,
                                    ),
                                )
                            }
                        },
                        modifier = Modifier
                            .testTag("reciter_${voice.id}")
                            .clickable { onPick(voice) },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                }
            }
        }
    }
}

internal fun formatBytes(context: Context, bytes: Long): String =
    Formatter.formatShortFileSize(context, bytes)
