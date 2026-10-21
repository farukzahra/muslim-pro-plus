package com.farukzahra.muslimplus.ui.screens.prayers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.location.CountryNames
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import java.time.format.DateTimeFormatter

private val tablePrayers = listOf(
    Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA,
)
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun PrayersScreen(viewModel: PrayersViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val current = state ?: return
    var showCities by remember { mutableStateOf(false) }
    var showMethods by remember { mutableStateOf(false) }
    var showOffsets by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            OutlinedButton(
                onClick = { showCities = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
            ) {
                Text(current.city.name, maxLines = 1)
            }
            OutlinedButton(
                onClick = { showMethods = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
            ) {
                Text(current.method.name, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        ) {
            OutlinedButton(onClick = { showOffsets = true }) {
                Text(stringResource(R.string.prayers_adjust))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "",
                modifier = Modifier.weight(0.9f),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            tablePrayers.forEach { prayer ->
                Text(
                    text = stringResource(prayerLabel(prayer)),
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(current.days) { day ->
                val isToday = day.date == current.today
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isToday) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        modifier = Modifier.weight(0.9f),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    tablePrayers.forEach { prayer ->
                        Text(
                            text = day.times[prayer]?.format(timeFormat).orEmpty(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = if (isToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }
        }
    }

    if (showCities) {
        CityDialog(
            onSearch = viewModel::searchCities,
            onPick = { viewModel.selectCity(it) },
            onDismiss = { showCities = false },
        )
    }
    if (showMethods) {
        ChoiceDialog(
            title = stringResource(R.string.prayers_method),
            options = PrayerMethod.entries.map { it.name to it.name },
            onPick = { viewModel.selectMethod(it) },
            onDismiss = { showMethods = false },
        )
    }
    if (showOffsets) {
        OffsetsDialog(
            offsets = current.offsets,
            onAdjust = { prayer, delta -> viewModel.adjustOffset(prayer, delta) },
            onDismiss = { showOffsets = false },
        )
    }
}

@Composable
private fun OffsetsDialog(
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
                            text = stringResource(prayerLabel(prayer)),
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

private fun offsetLabel(minutes: Int): String =
    if (minutes > 0) "+$minutes" else minutes.toString()

@Composable
private fun CityDialog(
    onSearch: (String) -> List<City>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val locale = LocalConfiguration.current.locales[0]
    // Busca sincrona no catalogo em memoria; 3000 nomes pre-normalizados.
    val results = remember(query) { onSearch(query) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prayers_city)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.prayers_city_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (results.isEmpty()) {
                    Text(
                        text = stringResource(R.string.prayers_city_none),
                        modifier = Modifier.padding(vertical = 16.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .padding(top = 8.dp),
                    ) {
                        items(results, key = { it.id }) { city ->
                            Text(
                                text = stringResource(
                                    R.string.city_with_country,
                                    city.name,
                                    CountryNames.display(city.countryCode, locale),
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPick(city.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp),
                                fontSize = 16.sp,
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(options) { (id, label) ->
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPick(id)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp,
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun prayerLabel(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
