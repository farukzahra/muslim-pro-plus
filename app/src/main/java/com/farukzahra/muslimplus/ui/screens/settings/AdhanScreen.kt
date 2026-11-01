package com.farukzahra.muslimplus.ui.screens.settings

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanCountry
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerReminder
import kotlinx.coroutines.launch

private enum class SoundSheet { Default, Fajr }

/**
 * Configuração do chamado: quais orações tocam, a gravação padrão, a gravação
 * própria do Fajr e as opções de silêncio e lembrete.
 *
 * A escolha da voz abre uma bottom sheet. Com 22 gravações, uma lista inline
 * deixaria a tela com quase quarenta linhas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val prayers by settings.adhanPrayers.collectAsState(initial = AdhanPolicy.defaultEnabled)
    val defaultId by settings.adhanSoundId.collectAsState(initial = AdhanSound.default.id)
    val fajrId by settings.adhanFajrSoundId.collectAsState(initial = AdhanSound.defaultFajr.id)
    val fajrOverride by settings.adhanFajrOverride.collectAsState(initial = true)
    val showMigrationNotice by settings.adhanFajrNotice.collectAsState(initial = false)
    val respectSilent by settings.adhanRespectSilent.collectAsState(initial = false)
    val reminderMinutes by settings.adhanReminderMinutes.collectAsState(initial = PrayerReminder.OFF)

    val defaultSound = AdhanSound.findNormal(defaultId)
    val fajrSound = AdhanSound.findFajr(fajrId)

    var sheet by remember { mutableStateOf<SoundSheet?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }

    fun stopPreview() {
        player?.release()
        player = null
        playingId = null
    }

    fun togglePreview(sound: AdhanSound) {
        if (playingId == sound.id) {
            stopPreview()
            return
        }
        stopPreview()
        player = MediaPlayer.create(context, sound.resId)?.apply {
            setOnCompletionListener { stopPreview() }
            start()
        }
        playingId = sound.id
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

            if (showMigrationNotice) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .testTag("adhan_fajr_notice"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.adhan_fajr_note),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(
                            onClick = { scope.launch { settings.dismissAdhanFajrNotice() } },
                            modifier = Modifier.testTag("adhan_fajr_notice_dismiss"),
                        ) {
                            Text(stringResource(R.string.adhan_fajr_note_action))
                        }
                    }
                }
            }

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
                            scope.launch {
                                settings.setAdhanPrayers(next)
                                PrayerScheduler(context).reschedule()
                            }
                        },
                        modifier = Modifier.testTag("adhan_${prayer.name.lowercase()}"),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.adhan_sound_default_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.adhan_sound_default_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            SoundCard(
                sound = defaultSound,
                onChange = { sheet = SoundSheet.Default },
                cardTag = "adhan_default_card",
                changeTag = "adhan_change_default",
                modifier = Modifier.padding(top = 10.dp),
            )

            Text(
                text = stringResource(R.string.adhan_sound_fajr_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.adhan_sound_override),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.adhan_sound_override_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = fajrOverride,
                    onCheckedChange = { scope.launch { settings.setAdhanFajrOverride(it) } },
                    modifier = Modifier.testTag("adhan_fajr_override"),
                )
            }
            if (fajrOverride) {
                Text(
                    text = stringResource(R.string.adhan_sound_fajr_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                SoundCard(
                    sound = fajrSound,
                    onChange = { sheet = SoundSheet.Fajr },
                    cardTag = "adhan_fajr_card",
                    changeTag = "adhan_change_fajr",
                    modifier = Modifier.padding(top = 10.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.adhan_sound_fajr_same),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text(
                text = stringResource(R.string.adhan_volume_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = stringResource(R.string.adhan_stop_triggers),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.adhan_reminder_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.adhan_reminder_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            ReminderChoices(
                selected = reminderMinutes,
                onPick = { minutes ->
                    scope.launch {
                        settings.setAdhanReminderMinutes(minutes)
                        PrayerScheduler(context).reschedule()
                    }
                },
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

    sheet?.let { target ->
        val options = if (target == SoundSheet.Fajr) AdhanSound.fajr else AdhanSound.normal
        val selectedId = if (target == SoundSheet.Fajr) fajrId else defaultId
        ModalBottomSheet(
            onDismissRequest = {
                stopPreview()
                sheet = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = stringResource(
                        if (target == SoundSheet.Fajr) {
                            R.string.adhan_sound_choose_fajr
                        } else {
                            R.string.adhan_sound_choose
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(
                        if (target == SoundSheet.Fajr) {
                            R.string.adhan_sound_fajr_hint
                        } else {
                            R.string.adhan_sound_default_hint
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                ) {
                    items(options, key = { it.id }) { sound ->
                        SoundOptionRow(
                            sound = sound,
                            selected = sound.id == selectedId,
                            playing = playingId == sound.id,
                            onPreview = { togglePreview(sound) },
                            onSelect = {
                                stopPreview()
                                scope.launch {
                                    if (target == SoundSheet.Fajr) {
                                        settings.setAdhanFajrSound(sound.id)
                                    } else {
                                        settings.setAdhanSound(sound.id)
                                    }
                                }
                                sheet = null
                            },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SoundCard(
    sound: AdhanSound,
    onChange: () -> Unit,
    cardTag: String,
    changeTag: String,
    modifier: Modifier = Modifier,
) {
    val country = sound.country?.let { stringResource(it.labelRes) }
    val meta = listOfNotNull(country, durationText(sound.durationSeconds)).joinToString(" · ")
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(cardTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountryFlag(sound.country)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(sound.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = onChange,
                modifier = Modifier.testTag(changeTag),
            ) {
                Text(stringResource(R.string.adhan_sound_change))
            }
        }
    }
}

@Composable
private fun SoundOptionRow(
    sound: AdhanSound,
    selected: Boolean,
    playing: Boolean,
    onPreview: () -> Unit,
    onSelect: () -> Unit,
) {
    val country = sound.country?.let { stringResource(it.labelRes) }
    val meta = listOfNotNull(country, durationText(sound.durationSeconds)).joinToString(" · ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("adhan_sound_option_${sound.id}")
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        CountryFlag(sound.country)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(sound.labelRes),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (sound.fajrOnly) {
                    "$meta · ${stringResource(R.string.adhan_sound_tathwib)}"
                } else {
                    meta
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(
            onClick = onPreview,
            modifier = Modifier.testTag("adhan_preview_${sound.id}"),
        ) {
            Text(
                stringResource(if (playing) R.string.adhan_stop else R.string.adhan_preview),
            )
        }
    }
}

@Composable
private fun CountryFlag(country: AdhanCountry?) {
    if (country == null) {
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "♪",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Image(
            painter = painterResource(country.flagRes),
            contentDescription = stringResource(country.labelRes),
            modifier = Modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(3.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ReminderChoices(selected: Int, onPick: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PrayerReminder.options.forEach { minutes ->
            FilterChip(
                selected = minutes == selected,
                onClick = { onPick(minutes) },
                label = {
                    Text(
                        text = if (minutes == PrayerReminder.OFF) {
                            stringResource(R.string.adhan_reminder_off)
                        } else {
                            stringResource(R.string.adhan_reminder_minutes, minutes)
                        },
                    )
                },
                modifier = Modifier.testTag("adhan_reminder_$minutes"),
            )
        }
    }
}

private fun durationText(seconds: Int): String =
    "%d:%02d".format(seconds / 60, seconds % 60)

private fun prayerLabel(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
