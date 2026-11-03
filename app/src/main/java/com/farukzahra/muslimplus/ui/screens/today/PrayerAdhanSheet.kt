package com.farukzahra.muslimplus.ui.screens.today

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.adhan.AdhanCountry
import com.farukzahra.muslimplus.adhan.AdhanOverrides
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Estado efetivo da oração: o que a sheet mostra antes de qualquer edição.
 * `null` em um override significa "herda o global".
 */
data class PrayerAdhanState(
    val notifyOn: Boolean,
    val sound: AdhanSound,
    val soundOverride: AdhanSound?,
    val reminder: Int,
    val reminderOverride: Int?,
    val alarm: Boolean,
    val days: Set<DayOfWeek>,
    val globalSound: AdhanSound,
    val globalReminder: Int,
)

/** O que o usuário confirmou ao tocar em Salvar. */
data class PrayerAdhanEdit(
    val notifyOn: Boolean,
    val soundOverride: AdhanSound?,
    val reminderOverride: Int?,
    val alarm: Boolean,
    val days: Set<DayOfWeek>,
)

/**
 * Lê o estado efetivo da oração e só então monta a sheet, para o rascunho
 * nascer dos valores certos e não dos padrões.
 */
@Composable
fun PrayerAdhanSheetHost(prayer: Prayer, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val snapshot by produceState<PrayerAdhanState?>(initialValue = null, prayer) {
        val enabled = prayer in settings.adhanPrayers.first()
        val globalSound = AdhanSound.findNormal(settings.adhanSoundId.first())
        val fajrSound = AdhanSound.findFajr(settings.adhanFajrSoundId.first())
        val fajrOverride = settings.adhanFajrOverride.first()
        val sounds = settings.adhanSoundsByPrayer.first()
        val globalReminder = settings.adhanReminderMinutes.first()
        val reminders = settings.adhanRemindersByPrayer.first()
        val soundOverride = sounds[prayer]
        val reminderOverride = reminders[prayer]
        value = PrayerAdhanState(
            notifyOn = enabled,
            sound = AdhanPolicy.resolveFor(
                prayer, globalSound, fajrSound, fajrOverride, sounds,
            ),
            soundOverride = soundOverride,
            reminder = PrayerReminder.minutesFor(prayer, globalReminder, reminders),
            reminderOverride = reminderOverride,
            alarm = prayer in settings.adhanAlarmByPrayer.first(),
            days = settings.adhanRepeatsByPrayer.first()[prayer] ?: AdhanOverrides.allDays,
            globalSound = globalSound,
            globalReminder = globalReminder,
        )
    }

    val state = snapshot ?: return
    PrayerAdhanSheet(
        prayer = prayer,
        initial = state,
        onSave = { edit ->
            scope.launch {
                save(prayer, edit)
                PrayerScheduler(context).reschedule()
                // Fecha só depois de gravar: a sheet e o escopo morrem juntos.
                onDismiss()
            }
        },
        onDismiss = onDismiss,
    )
}

private suspend fun save(prayer: Prayer, edit: PrayerAdhanEdit) {
    val settings = ServiceLocator.settings
    val prayers = settings.adhanPrayers.first().toMutableSet()
    if (edit.notifyOn) prayers += prayer else prayers -= prayer
    settings.setAdhanPrayers(prayers)

    val sounds = settings.adhanSoundsByPrayer.first().toMutableMap()
    if (edit.soundOverride != null) sounds[prayer] = edit.soundOverride else sounds.remove(prayer)
    settings.setAdhanSoundsByPrayer(sounds)

    val reminders = settings.adhanRemindersByPrayer.first().toMutableMap()
    if (edit.reminderOverride != null) {
        reminders[prayer] = edit.reminderOverride
    } else {
        reminders.remove(prayer)
    }
    settings.setAdhanRemindersByPrayer(reminders)

    val alarm = settings.adhanAlarmByPrayer.first().toMutableSet()
    if (edit.alarm) alarm += prayer else alarm -= prayer
    settings.setAdhanAlarmByPrayer(alarm)

    val repeats = settings.adhanRepeatsByPrayer.first().toMutableMap()
    if (edit.days.isEmpty() || edit.days == AdhanOverrides.allDays) {
        repeats.remove(prayer)
    } else {
        repeats[prayer] = edit.days
    }
    settings.setAdhanRepeatsByPrayer(repeats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerAdhanSheet(
    prayer: Prayer,
    initial: PrayerAdhanState,
    onSave: (PrayerAdhanEdit) -> Unit,
    onDismiss: () -> Unit,
) {
    var notifyOn by remember(prayer) { mutableStateOf(initial.notifyOn) }
    var soundOverride by remember(prayer) { mutableStateOf(initial.soundOverride) }
    var reminderOverride by remember(prayer) { mutableStateOf(initial.reminderOverride) }
    var alarm by remember(prayer) { mutableStateOf(initial.alarm) }
    var days by remember(prayer) { mutableStateOf(initial.days) }
    var view by remember(prayer) { mutableStateOf(SheetView.Main) }

    val context = LocalContext.current
    val locale = Locale.getDefault()
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

    val sound = soundOverride ?: initial.sound
    val reminder = reminderOverride ?: initial.reminder
    val changed = notifyOn != initial.notifyOn ||
        soundOverride != initial.soundOverride ||
        reminderOverride != initial.reminderOverride ||
        alarm != initial.alarm ||
        days != initial.days

    ModalBottomSheet(
        onDismissRequest = {
            stopPreview()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("prayer_adhan_sheet"),
    ) {
        when (view) {
            SheetView.Main -> SheetMain(
                prayer = prayer,
                notifyOn = notifyOn,
                onNotifyChange = { notifyOn = it },
                alarm = alarm,
                onAlarmChange = { alarm = it },
                days = days,
                onToggleDay = { day ->
                    val next = if (day in days) days - day else days + day
                    // Desmarcar o último dia volta para "todos", para não
                    // silenciar a oração para sempre por engano.
                    days = next.ifEmpty { AdhanOverrides.allDays }
                },
                soundLabel = stringResource(sound.labelRes),
                soundIsDefault = soundOverride == null,
                reminder = reminder,
                reminderIsDefault = reminderOverride == null,
                onOpenSounds = { view = SheetView.Sounds },
                onOpenReminder = { view = SheetView.Reminders },
                changed = changed,
                onSave = {
                    stopPreview()
                    onSave(PrayerAdhanEdit(notifyOn, soundOverride, reminderOverride, alarm, days))
                },
                onDismiss = {
                    stopPreview()
                    onDismiss()
                },
                locale = locale,
            )

            SheetView.Sounds -> SoundList(
                options = if (prayer == Prayer.FAJR) AdhanSound.fajr else AdhanSound.normal,
                globalLabel = stringResource(initial.globalSound.labelRes),
                selectedId = soundOverride?.id,
                playingId = playingId,
                onPreview = ::togglePreview,
                onDefault = {
                    stopPreview()
                    soundOverride = null
                    view = SheetView.Main
                },
                onPick = {
                    stopPreview()
                    soundOverride = it
                    view = SheetView.Main
                },
                onBack = {
                    stopPreview()
                    view = SheetView.Main
                },
            )

            SheetView.Reminders -> ReminderList(
                globalMinutes = initial.globalReminder,
                selected = reminderOverride,
                onDefault = {
                    reminderOverride = null
                    view = SheetView.Main
                },
                onPick = {
                    reminderOverride = it
                    view = SheetView.Main
                },
                onBack = { view = SheetView.Main },
            )
        }
    }
}

private enum class SheetView { Main, Sounds, Reminders }


@Composable
private fun SheetMain(
    prayer: Prayer,
    notifyOn: Boolean,
    onNotifyChange: (Boolean) -> Unit,
    alarm: Boolean,
    onAlarmChange: (Boolean) -> Unit,
    days: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek) -> Unit,
    soundLabel: String,
    soundIsDefault: Boolean,
    reminder: Int,
    reminderIsDefault: Boolean,
    onOpenSounds: () -> Unit,
    onOpenReminder: () -> Unit,
    changed: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    locale: Locale,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.prayer_adhan_sheet_title,
                    stringResource(prayerLabelRes(prayer)),
                ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("prayer_adhan_close"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.prayer_adhan_close),
                )
            }
        }

        SwitchRow(
            title = stringResource(R.string.prayer_adhan_notify),
            hint = stringResource(R.string.prayer_adhan_notify_hint),
            checked = notifyOn,
            onCheckedChange = onNotifyChange,
            tag = "prayer_adhan_notify",
        )
        SwitchRow(
            title = stringResource(R.string.prayer_adhan_alarm),
            hint = stringResource(R.string.prayer_adhan_alarm_hint),
            checked = alarm,
            onCheckedChange = onAlarmChange,
            tag = "prayer_adhan_alarm",
        )

        Text(
            text = stringResource(R.string.prayer_adhan_repeats),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in days,
                    onClick = { onToggleDay(day) },
                    label = {
                        Text(
                            text = day.getDisplayName(TextStyle.NARROW, locale)
                                .replaceFirstChar { it.titlecase(locale) },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prayer_adhan_day_${day.value}"),
                )
            }
        }
        Text(
            text = if (days.size == DayOfWeek.entries.size) {
                stringResource(R.string.prayer_adhan_repeats_all)
            } else {
                days.sortedBy { it.value }
                    .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )
        ValueRow(
            title = stringResource(R.string.prayer_adhan_sound_row),
            value = soundLabel,
            defaulted = soundIsDefault,
            onClick = onOpenSounds,
            tag = "prayer_adhan_sound_row",
        )
        ValueRow(
            title = stringResource(R.string.prayer_adhan_reminder_row),
            value = reminderText(reminder),
            defaulted = reminderIsDefault,
            onClick = onOpenReminder,
            tag = "prayer_adhan_reminder_row",
        )

        Button(
            onClick = onSave,
            enabled = changed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .testTag("prayer_adhan_save"),
        ) {
            Text(stringResource(R.string.prayer_adhan_save))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    hint: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
        )
    }
}

@Composable
private fun ValueRow(
    title: String,
    value: String,
    defaulted: Boolean,
    onClick: () -> Unit,
    tag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (defaulted) {
                Text(
                    text = stringResource(R.string.prayer_adhan_inherits_default),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundList(
    options: List<AdhanSound>,
    globalLabel: String,
    selectedId: String?,
    playingId: String?,
    onPreview: (AdhanSound) -> Unit,
    onDefault: () -> Unit,
    onPick: (AdhanSound) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Text(
                text = stringResource(R.string.adhan_sound_choose),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDefault)
                .testTag("prayer_sound_default")
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selectedId == null, onClick = onDefault)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.prayer_adhan_use_default_sound, globalLabel),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)) {
            items(options, key = { it.id }) { item ->
                SoundOption(
                    sound = item,
                    selected = item.id == selectedId,
                    playing = item.id == playingId,
                    onPreview = { onPreview(item) },
                    onPick = { onPick(item) },
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SoundOption(
    sound: AdhanSound,
    selected: Boolean,
    playing: Boolean,
    onPreview: () -> Unit,
    onPick: () -> Unit,
) {
    val country = sound.country?.let { stringResource(it.labelRes) }
    val meta = listOfNotNull(country, durationText(sound.durationSeconds)).joinToString(" · ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPick)
            .testTag("prayer_sound_option_${sound.id}")
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onPick)
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
            modifier = Modifier.testTag("prayer_preview_${sound.id}"),
        ) {
            Text(stringResource(if (playing) R.string.adhan_stop else R.string.adhan_preview))
        }
    }
}

@Composable
private fun ReminderList(
    globalMinutes: Int,
    selected: Int?,
    onDefault: () -> Unit,
    onPick: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Text(
                text = stringResource(R.string.prayer_adhan_reminder_row),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        ReminderOption(
            label = stringResource(
                R.string.prayer_adhan_use_default_reminder,
                reminderText(globalMinutes),
            ),
            selected = selected == null,
            onClick = onDefault,
            tag = "prayer_reminder_default",
        )
        PrayerReminder.options.forEach { minutes ->
            ReminderOption(
                label = reminderText(minutes),
                selected = selected == minutes,
                onClick = { onPick(minutes) },
                tag = "prayer_reminder_$minutes",
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReminderOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.titleSmall)
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
        )
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

@Composable
private fun reminderText(minutes: Int): String =
    if (PrayerReminder.isOff(minutes)) {
        stringResource(R.string.prayer_adhan_off)
    } else {
        stringResource(R.string.adhan_reminder_minutes, minutes)
    }

private fun durationText(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

private fun prayerLabelRes(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
