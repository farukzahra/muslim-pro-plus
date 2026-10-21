package com.farukzahra.muslimplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerOffsets

/**
 * Ajuste de minutos por oracao, usado na tela Oracoes e em Configuracoes.
 *
 * O valor exibido vem de [offsets] (persistido no DataStore); cada toque
 * chama [onAdjust] com +1/-1, e a soma acontece dentro do `edit`, entao toques
 * seguidos nao se perdem.
 */
@Composable
fun OffsetsDialog(
    offsets: Map<Prayer, Int>,
    onAdjust: (Prayer, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prayers_offsets_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.prayers_offsets_hint),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrayerOffsets.adjustable.forEach { prayer ->
                    val minutes = offsets[prayer] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                        )
                        TextButton(
                            onClick = { onAdjust(prayer, -1) },
                            enabled = minutes > PrayerOffsets.MIN_MINUTES,
                        ) {
                            Text("−", fontSize = 20.sp)
                        }
                        Text(
                            text = offsetLabel(minutes),
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                        )
                        TextButton(
                            onClick = { onAdjust(prayer, +1) },
                            enabled = minutes < PrayerOffsets.MAX_MINUTES,
                        ) {
                            Text("+", fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok))
            }
        },
    )
}

/** Rotulo da oracao nos tres idiomas. */
fun prayerLabelRes(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}

/** `0`, `+n` ou `-n`; o sinal faz parte do valor, nao e separador. */
fun offsetLabel(minutes: Int): String =
    if (minutes > 0) "+$minutes" else minutes.toString()
