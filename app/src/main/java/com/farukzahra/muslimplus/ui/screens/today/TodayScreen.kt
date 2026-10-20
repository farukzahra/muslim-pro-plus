package com.farukzahra.muslimplus.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerSchedule
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun TodayScreen(viewModel: TodayViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value ?: return
    val locale = LocalConfiguration.current.locales[0]

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
            dateText = dateText,
            hijriText = hijriText,
            city = state.city.name,
            next = state.next,
            nextAt = state.nextAt ?: state.tomorrowFajr,
            now = state.now,
            zone = state.zone,
            locale = locale,
        )
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
    val primary = MaterialTheme.colorScheme.primary
    Column(        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(primary, primary.copy(alpha = 0.72f)),
                ),
            )
            .padding(horizontal = 24.dp, vertical = 22.dp),
    ) {
        Text(
            text = dateText.replaceFirstChar { it.titlecase(locale) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = "$hijriText · $city",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(18.dp))
        if (nextAt != null) {
            Text(
                text = stringResource(R.string.today_next),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
            Text(
                text = if (next == null) {
                    stringResource(R.string.today_tomorrow_fajr)
                } else {
                    stringResource(prayerLabel(next))
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = formatCountdown(remainingSeconds(now, nextAt)),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = nextAt.atZone(zone).format(
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
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
        items(Prayer.entries) { prayer ->
            val at = schedule.timeOf(prayer)
            val isNext = prayer == next
            val isPast = at != null && at.isBefore(now)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(prayerLabel(prayer)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> MaterialTheme.colorScheme.primary
                        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = at?.atZone(zone)?.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale),
                    ).orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> MaterialTheme.colorScheme.primary
                        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
