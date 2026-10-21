package com.farukzahra.muslimplus.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.notifications.ExactAlarms
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerSchedule
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun TodayScreen(viewModel: TodayViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value ?: return
    val locale = LocalConfiguration.current.locales[0]

    // Permissao de alarme exato pode ter mudado nas configuracoes do sistema.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshExactAlarms()
        onPauseOrDispose { }
    }

    val dateText = remember(state.date, locale) {
        state.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
    }
    val hijriText = remember(state.date, locale) {
        runCatching {
            val hijri = HijrahDate.from(state.date)
            hijri.format(
                DateTimeFormatter.ofPattern("d MMMM y", locale).withChronology(hijri.chronology),
            )
        }.getOrDefault("")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            dateText = dateText.replaceFirstChar { it.titlecase(locale) },
            hijriText = hijriText,
            city = state.city.name,
            next = state.next,
            nextAt = state.nextAt ?: state.tomorrowFajr,
            now = state.now,
            zone = state.zone,
            locale = locale,
        )
        if (!state.exactAlarmsAvailable) {
            ExactAlarmBanner()
        }
        PrayerList(
            schedule = state.schedule,
            next = state.next,
            now = state.now,
            zone = state.zone,
            locale = locale,
        )
    }
}

@Composable
private fun Header(
    dateText: String,
    hijriText: String,
    city: String,
    next: Prayer?,
    nextAt: Instant?,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(scheme.primaryContainer, scheme.background),
                ),
            ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = "$hijriText · $city",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(26.dp))
            if (nextAt != null) {
                Text(
                    text = stringResource(R.string.today_next).uppercase(locale),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.secondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (next == null) {
                        stringResource(R.string.today_tomorrow_fajr)
                    } else {
                        stringResource(prayerLabel(next))
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onSurface,
                )
                Text(
                    text = formatCountdown(remainingSeconds(now, nextAt)),
                    style = MaterialTheme.typography.displayLarge,
                    color = scheme.primary,
                )
                Text(
                    text = nextAt.atZone(zone).format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ExactAlarmBanner() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.secondaryContainer)
            .padding(start = 24.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.today_exact_alarm_warning),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = {
                runCatching { context.startActivity(ExactAlarms.requestIntent(context)) }
            },
        ) {
            Text(stringResource(R.string.action_allow))
        }
    }
}

@Composable
private fun PrayerList(
    schedule: PrayerSchedule,
    next: Prayer?,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(Prayer.entries) { index, prayer ->
            val at = schedule.timeOf(prayer)
            val isNext = prayer == next
            val isPast = at != null && at.isBefore(now)
            val scheme = MaterialTheme.colorScheme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isNext) scheme.primary.copy(alpha = 0.08f) else scheme.background)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isNext) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isNext -> scheme.primary
                                    isPast -> scheme.outline.copy(alpha = 0.5f)
                                    else -> scheme.outline
                                },
                            ),
                    )
                    if (index < Prayer.entries.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(scheme.outlineVariant),
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = stringResource(prayerLabel(prayer)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> scheme.primary
                        isPast -> scheme.onSurface.copy(alpha = 0.4f)
                        else -> scheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = at?.atZone(zone)?.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale),
                    ).orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> scheme.primary
                        isPast -> scheme.onSurface.copy(alpha = 0.4f)
                        else -> scheme.onSurface
                    },
                )
            }
        }
    }
}

private fun remainingSeconds(now: Instant, target: Instant?): Long =
    if (target == null) 0 else Duration.between(now, target).seconds.coerceAtLeast(0)

private fun formatCountdown(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun prayerLabel(prayer: Prayer): Int = when (prayer) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
