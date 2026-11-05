package com.farukzahra.muslimplus.ui.screens.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.notifications.ExactAlarms
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerDay
import com.farukzahra.muslimplus.prayer.PrayerLog
import com.farukzahra.muslimplus.prayer.PrayerSchedule
import com.farukzahra.muslimplus.quran.DailyVerse
import com.farukzahra.muslimplus.quran.Verse
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoField
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = viewModel(),
    onOpenVerse: (Int, Int) -> Unit = { _, _ -> },
) {
    val state = viewModel.state.collectAsState().value ?: return
    val locale = LocalConfiguration.current.locales[0]
    val showDailyVerse by ServiceLocator.settings.dailyVerseVisible.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    var adhanSheetPrayer by remember { mutableStateOf<Prayer?>(null) }

    // Permissao de alarme exato pode ter mudado nas configuracoes do sistema.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshExactAlarms()
        onPauseOrDispose { }
    }

    val dateText = remember(state.date, locale) {
        state.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
    }
    // Mês hijri vem dos nossos recursos: o ICU devolve "Jumada I" em inglês
    // mesmo com o app em português, e a mistura de idioma quebrava a leitura.
    val hijri = remember(state.date, state.hijriOffset) {
        runCatching { HijrahDate.from(state.date.plusDays(state.hijriOffset.toLong())) }.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Header(
            dateText = dateText.replaceFirstChar { it.titlecase(locale) },
            hijriText = hijri,
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
        if (showDailyVerse) {
            DailyVerseCard(
                date = state.date,
                onHide = {
                    scope.launch { ServiceLocator.settings.setDailyVerseVisible(false) }
                },
                onOpen = onOpenVerse,
            )
        } else {
            TextButton(
                onClick = {
                    scope.launch { ServiceLocator.settings.setDailyVerseVisible(true) }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("daily_verse_show"),
            ) {
                Text(stringResource(R.string.daily_verse_show))
            }
        }
        PrayerTracker(
            date = state.date,
            done = state.done,
            week = state.week,
            streak = state.streak,
            locale = locale,
            onMarkAll = viewModel::markAllPrayers,
        )
        PrayerList(
            schedule = state.schedule,
            next = state.next,
            now = state.now,
            zone = state.zone,
            locale = locale,
            done = state.done,
            adhanPrayers = state.adhanPrayers,
            onTogglePrayer = viewModel::togglePrayer,
            onOpenAdhan = { adhanSheetPrayer = it },
        )
        Spacer(Modifier.height(12.dp))
    }

    adhanSheetPrayer?.let { prayer ->
        PrayerAdhanSheetHost(prayer = prayer, onDismiss = { adhanSheetPrayer = null })
    }
}

@Composable
private fun Header(
    dateText: String,
    hijriText: HijrahDate?,
    city: String,
    next: Prayer?,
    nextAt: Instant?,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
) {
    val scheme = MaterialTheme.colorScheme
    val hijriLine = hijriText?.let { hijri ->
        stringResource(
            R.string.hijri_date,
            hijri.get(ChronoField.DAY_OF_MONTH),
            stringResource(hijriMonthLabel(hijri.get(ChronoField.MONTH_OF_YEAR))),
            hijri.get(ChronoField.YEAR),
        )
    }.orEmpty()
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
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
            Text(
                text = stringResource(R.string.today_hijri_city, hijriLine, city),
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
                    modifier = Modifier.testTag("today_countdown"),
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

/**
 * Rastreador do dia: anel `n/5`, sequência e faixa dos últimos sete dias.
 * O botão "marcar todas" some quando o dia já está completo.
 */
@Composable
private fun hijriMonthLabel(month: Int): Int = when (month) {
    1 -> R.string.hijri_month_1
    2 -> R.string.hijri_month_2
    3 -> R.string.hijri_month_3
    4 -> R.string.hijri_month_4
    5 -> R.string.hijri_month_5
    6 -> R.string.hijri_month_6
    7 -> R.string.hijri_month_7
    8 -> R.string.hijri_month_8
    9 -> R.string.hijri_month_9
    10 -> R.string.hijri_month_10
    11 -> R.string.hijri_month_11
    else -> R.string.hijri_month_12
}
@Composable
private fun PrayerTracker(
    date: LocalDate,
    done: Set<Prayer>,
    week: List<PrayerDay>,
    streak: Int,
    locale: Locale,
    onMarkAll: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val total = PrayerLog.trackable.size
    val count = PrayerLog.progress(done)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.75f))
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 14.dp)
            .testTag("tracker_card"),
    ) {
        // Titulo e acao na mesma linha; o anel e o texto ficam abaixo, para o
        // texto nao ser espremido entre o anel e o botao em telas estreitas.
        // A acao tem sempre o mesmo tamanho, com botao ou check, e o titulo
        // nunca quebra: o card nao pode mudar de altura ao marcar/desmarcar.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.today_tracker_title),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .testTag("tracker_title"),
            )
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (count < total) {
                    IconButton(
                        onClick = onMarkAll,
                        modifier = Modifier.testTag("tracker_mark_all"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = stringResource(R.string.today_mark_all),
                            tint = scheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.today_tracker_done),
                        tint = scheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("tracker_done"),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(count = count, total = total, modifier = Modifier.size(56.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_tracker_progress, count, total),
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    modifier = Modifier.testTag("tracker_progress"),
                )
                Text(
                    text = if (streak > 0) {
                        stringResource(
                            if (streak == 1) {
                                R.string.today_tracker_streak_one
                            } else {
                                R.string.today_tracker_streak
                            },
                            streak,
                        )
                    } else {
                        stringResource(R.string.today_tracker_streak_none)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tracker_week"),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            week.forEach { day ->
                WeekDay(
                    day = day,
                    today = date,
                    locale = locale,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Espaço reservado sempre: some a dica, mas o card não muda de altura.
        Box(modifier = Modifier.fillMaxWidth().height(32.dp)) {
            if (count == 0) {
                Text(
                    text = stringResource(R.string.today_tracker_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("tracker_hint"),
                )
            }
        }
    }
}

@Composable
private fun WeekDay(day: PrayerDay, today: LocalDate, locale: Locale, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isToday = day.date == today
    val label = remember(day.date, locale) {
        day.date.getDayOfWeek().getDisplayName(JavaTextStyle.NARROW, locale)
    }
    val count = PrayerLog.progress(day.done)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("tracker_day_${day.date}"),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) scheme.primary else scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.complete -> scheme.primary
                        count > 0 -> scheme.primary.copy(alpha = 0.18f)
                        else -> scheme.background
                    },
                )
                .border(
                    BorderStroke(
                        width = if (isToday) 2.dp else 1.dp,
                        color = when {
                            day.complete -> scheme.primary
                            isToday -> scheme.primary
                            else -> scheme.outlineVariant
                        },
                    ),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (day.complete) scheme.onPrimary else scheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProgressRing(count: Int, total: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.testTag("tracker_ring"), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = scheme.outlineVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (count > 0 && total > 0) {
                drawArc(
                    color = scheme.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * count / total,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = "$count/$total",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
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
private fun DailyVerseCard(date: LocalDate, onHide: () -> Unit, onOpen: (Int, Int) -> Unit) {
    val modes by ServiceLocator.settings.quranModes.collectAsState(initial = setOf("ar"))
    var refreshIndex by rememberSaveable(date) { mutableStateOf(0) }
    val index = DailyVerse.indexFor(date) + refreshIndex
    // Texto e camadas lidos fora da main thread, como no leitor.
    val data by produceState<Pair<Verse, Map<String, String>>?>(null, date, modes, index) {
        value = withContext(Dispatchers.IO) {
            val verse = DailyVerse.forIndex(index)
            val key = "${verse.surah}:${verse.ayah}"
            verse to modes.associateWith { mode ->
                ServiceLocator.quran.layer(mode, key).orEmpty()
            }
        }
    }
    val (current, layers) = data ?: return
    if (current.text.isBlank()) return
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 16.dp)
            .testTag("daily_verse_card"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.daily_verse_label),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.secondary,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { refreshIndex++ },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("daily_verse_refresh"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.daily_verse_refresh),
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onHide,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("daily_verse_hide"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.daily_verse_hide),
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        if ("ar" in modes) {
            Text(
                text = current.text,
                fontSize = 20.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Start,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = scheme.onSurface,
                    textDirection = TextDirection.Rtl,
                ),
            )
        }
        if ("translit" in modes) {
            layers["translit"]?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        // Barra entre o verso e a tradução, no mesmo traço do leitor.
        val translations = listOf("pt-BR", "en").mapNotNull { locale ->
            if (locale in modes) layers[locale]?.takeIf { it.isNotBlank() } else null
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp, end = 10.dp)
                .testTag("daily_verse_divider"),
            thickness = 1.dp,
            color = scheme.onSurface.copy(alpha = 0.14f),
        )
        translations.forEach {
            Text(
                text = it,
                fontSize = 15.sp,
                color = scheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onOpen(current.surah, current.ayah) },
                modifier = Modifier.testTag("daily_verse_open"),
            ) {
                Text(stringResource(R.string.daily_verse_open))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.verse_reference, current.surah, current.ayah),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .testTag("daily_verse_reference"),
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
    done: Set<Prayer>,
    adhanPrayers: Set<Prayer>,
    onTogglePrayer: (Prayer) -> Unit,
    onOpenAdhan: (Prayer) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Prayer.entries.forEachIndexed { index, prayer ->
            val at = schedule.timeOf(prayer)
            val isNext = prayer == next
            val isPast = at != null && at.isBefore(now)
            val trackable = prayer in PrayerLog.trackable
            val isDone = prayer in done
            val adhanOn = prayer in adhanPrayers
            val name = stringResource(prayerLabel(prayer))
            val checkDescription = if (isDone) {
                stringResource(R.string.today_unmark_prayer, name)
            } else {
                stringResource(R.string.today_mark_prayer, name)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(if (isNext) scheme.primary.copy(alpha = 0.08f) else scheme.background)
                    .clickable(enabled = trackable) { onTogglePrayer(prayer) }
                    .padding(horizontal = 20.dp)
                    .testTag("prayer_row_${prayer.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    val line = scheme.outlineVariant
                    when {
                        index == 0 && index < Prayer.entries.lastIndex -> Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxHeight(0.5f)
                                .width(1.dp)
                                .background(line),
                        )

                        index == Prayer.entries.lastIndex -> Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxHeight(0.5f)
                                .width(1.dp)
                                .background(line),
                        )

                        else -> Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(line),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(if (isNext) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isNext -> scheme.primary
                                    isDone -> scheme.primary.copy(alpha = 0.7f)
                                    isPast -> scheme.outline.copy(alpha = 0.5f)
                                    else -> scheme.outline
                                },
                            ),
                    )
                }
                if (trackable) {
                    IconButton(
                        onClick = { onTogglePrayer(prayer) },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("prayer_check_${prayer.name.lowercase()}")
                            .semantics { contentDescription = checkDescription },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDone) scheme.primary else scheme.background)
                                .border(
                                    BorderStroke(
                                        if (isDone) 2.dp else 1.5.dp,
                                        if (isDone) scheme.primary else scheme.outline,
                                    ),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = scheme.onPrimary,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.width(44.dp))
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> scheme.primary
                        isPast -> scheme.onSurface.copy(alpha = 0.45f)
                        else -> scheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                )
                if (trackable) {
                    IconButton(
                        onClick = { onOpenAdhan(prayer) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("prayer_bell_${prayer.name.lowercase()}"),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (adhanOn) R.drawable.ic_bell else R.drawable.ic_bell_off,
                            ),
                            contentDescription = stringResource(
                                R.string.prayer_adhan_open,
                                stringResource(prayerLabel(prayer)),
                            ),
                            tint = if (adhanOn) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Text(
                    text = at?.atZone(zone)?.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale),
                    ).orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> scheme.primary
                        isPast -> scheme.onSurface.copy(alpha = 0.45f)
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
