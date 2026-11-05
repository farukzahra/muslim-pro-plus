package com.farukzahra.muslimplus.ui.screens.settings

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.audio.QuranReciter
import com.farukzahra.muslimplus.quran.audio.QuranReciters

/**
 * Lista de vozes em uma folha, com qualidade e espaço já baixado. É a mesma
 * folha usada no leitor e na tela de áudio, para a troca de recitador ser um
 * toque em qualquer lugar.
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                            RadioButton(selected = voice.id == selectedId, onClick = null)
                        },
                        modifier = Modifier
                            .testTag("reciter_${voice.id}")
                            .clickable { onPick(voice) },
                    )
                }
            }
        }
    }
}

internal fun formatBytes(context: Context, bytes: Long): String =
    Formatter.formatShortFileSize(context, bytes)
