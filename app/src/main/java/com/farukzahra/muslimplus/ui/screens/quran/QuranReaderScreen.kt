package com.farukzahra.muslimplus.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef
import com.farukzahra.muslimplus.settings.ReaderFont
import com.farukzahra.muslimplus.settings.ReaderMode
import com.farukzahra.muslimplus.settings.ReaderPrefs
import com.farukzahra.muslimplus.settings.ReaderTheme
import com.farukzahra.muslimplus.ui.components.BookmarkFilled
import com.farukzahra.muslimplus.ui.components.BookmarkOutline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private data class LayerOption(val mode: String, val labelRes: Int)

private val layerOptions = listOf(
    LayerOption("ar", R.string.quran_arabic),
    LayerOption("translit", R.string.quran_transliteration),
    LayerOption("pt-BR", R.string.language_portuguese),
    LayerOption("en", R.string.language_english),
)

/** Paleta própria do leitor: tema de leitura não muda o resto do app. */
@Immutable
private data class ReaderPalette(
    val background: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val outline: Color,
) {
    val accentSoft: Color get() = accent.copy(alpha = 0.10f)
}

@Composable
private fun paletteFor(theme: ReaderTheme): ReaderPalette {
    val scheme = MaterialTheme.colorScheme
    return when (theme) {
        ReaderTheme.MODERN -> ReaderPalette(
            background = scheme.background,
            surface = scheme.surface,
            text = scheme.onSurface,
            muted = scheme.onSurfaceVariant,
            accent = scheme.primary,
            outline = scheme.outlineVariant,
        )
        ReaderTheme.PAPER -> ReaderPalette(
            background = Color(0xFFF4EBD8),
            surface = Color(0xFFFBF6E9),
            text = Color(0xFF2B2A23),
            muted = Color(0xFF6E6754),
            accent = Color(0xFF8A6A1F),
            outline = Color(0xFFD8CDB4),
        )
        ReaderTheme.DARK -> ReaderPalette(
            background = Color(0xFF0A1117),
            surface = Color(0xFF111C24),
            text = Color(0xFFE9EEF4),
            muted = Color(0xFF9FB0C2),
            accent = Color(0xFF2FB98A),
            outline = Color(0xFF243444),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    onBack: () -> Unit,
    onAudioSettings: () -> Unit = {},
    viewModel: QuranReaderViewModel = viewModel(),
) {
    val state = viewModel.state.collectAsState().value ?: return
    val modes by viewModel.modes.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val playback by viewModel.playback.collectAsState()
    val download by viewModel.download.collectAsState()
    val reciter by viewModel.reciter.collectAsState()
    val theme by viewModel.readerTheme.collectAsState()
    val font by viewModel.readerFont.collectAsState()
    val mode by viewModel.readerMode.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val palette = paletteFor(theme)
    val scale = font.scale

    KeepScreenOn(keepScreenOn)
    ReadingTimer(viewModel)

    // Restaura a posição uma única vez: depois disso, rolar é do usuário.
    LaunchedEffect(state.startIndex, mode) {
        if (mode == ReaderMode.LIST) {
            state.startIndex?.let { if (it > 0) listState.scrollToItem(it) }
        }
    }

    // Primeiro verso visível -> última leitura (o ViewModel aplica o debounce).
    LaunchedEffect(state.verses, mode) {
        if (mode != ReaderMode.LIST) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index -> state.verses.getOrNull(index)?.let(viewModel::onVerseVisible) }
    }

    // Avisa o fim do download (o progresso em si fica no topo).
    val doneMessage = stringResource(R.string.quran_audio_download_done)
    val failedMessage = stringResource(R.string.quran_audio_download_failed)
    val playbackFailed = stringResource(R.string.quran_audio_playback_failed)
    LaunchedEffect(playback.failed) {
        if (playback.failed) snackbarHostState.showSnackbar(playbackFailed)
    }
    val voiceMissingMessage = stringResource(R.string.audio_unknown_voice)
    val voiceSettingsAction = stringResource(R.string.audio_storage_title)
    LaunchedEffect(Unit) {
        viewModel.audioPrompt.collect {
            val result = snackbarHostState.showSnackbar(
                message = voiceMissingMessage,
                actionLabel = voiceSettingsAction,
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) onAudioSettings()
        }
    }
    var wasDownloading by remember { mutableStateOf(false) }
    LaunchedEffect(download.downloading, download.saved, download.failed) {
        if (wasDownloading && !download.downloading) {
            when {
                download.failed -> snackbarHostState.showSnackbar(failedMessage)
                download.saved -> snackbarHostState.showSnackbar(doneMessage)
            }
        }
        wasDownloading = download.downloading
    }

    Scaffold(
        containerColor = palette.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.surface,
                    titleContentColor = palette.text,
                    navigationIconContentColor = palette.text,
                    actionIconContentColor = palette.text,
                ),
                title = {
                    Column {
                        Text(
                            text = state.surah?.nameLatin.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = reciter?.reciter ?: stringResource(R.string.audio_unknown_voice),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    DownloadAction(
                        state = download,
                        onDownload = viewModel::downloadAudio,
                        onCancel = viewModel::cancelDownload,
                    )
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.quran_display),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (playback.surah != null) {
                MiniPlayer(
                    playback = playback,
                    surahName = state.surah?.nameLatin.orEmpty(),
                    reciterName = reciter?.reciter ?: stringResource(R.string.audio_unknown_voice),
                    palette = palette,
                    onPrevious = viewModel::previousVerse,
                    onToggle = viewModel::togglePlayback,
                    onNext = viewModel::nextVerse,
                    onSpeed = viewModel::cycleSpeed,
                )
            }
        },
    ) { padding ->
        when (mode) {
            ReaderMode.LIST -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(state.verses, key = { verse -> verse.ayah }) { verse ->
                    VerseBlock(
                        verse = verse,
                        modes = modes,
                        bookmarked = VerseRef(verse.surah, verse.ayah) in bookmarks,
                        playing = playback.surah == verse.surah && playback.ayah == verse.ayah,
                        anyPlaying = playback.playing,
                        palette = palette,
                        scale = scale,
                        onToggleBookmark = { viewModel.toggleBookmark(verse) },
                        onToggleAudio = { viewModel.onVerseAudio(verse) },
                        layer = { layerMode -> viewModel.layer(layerMode, verse) },
                    )
                    HorizontalDivider(color = palette.outline.copy(alpha = 0.4f))
                }
            }

            ReaderMode.PAGE -> VersePager(
                state = state,
                modes = modes,
                bookmarks = bookmarks,
                playback = playback,
                palette = palette,
                scale = scale,
                onVerseVisible = viewModel::onVerseVisible,
                onToggleBookmark = viewModel::toggleBookmark,
                onToggleAudio = viewModel::onVerseAudio,
                layer = viewModel::layer,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    if (showOptions) {
        ReaderSettingsSheet(
            palette = palette,
            theme = theme,
            font = font,
            mode = mode,
            keepScreenOn = keepScreenOn,
            modes = modes,
            onTheme = viewModel::setReaderTheme,
            onFont = viewModel::setReaderFont,
            onMode = viewModel::setReaderMode,
            onKeepScreenOn = viewModel::setKeepScreenOn,
            onToggleLayer = viewModel::toggle,
            onAudioSettings = {
                showOptions = false
                onAudioSettings()
            },
            onClose = { showOptions = false },
        )
    }
}

/** Mantém a tela ligada só enquanto o leitor está na frente. */
@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

/**
 * Cronometra a leitura em passos de 15 s apenas com a tela em primeiro plano.
 * Sem serviço de fundo e sem conta: é o suficiente para a meta diária.
 */
@Composable
private fun ReadingTimer(viewModel: QuranReaderViewModel) {
    val day by viewModel.readingDay.collectAsState()
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(lifecycle, day) {
        val date = day ?: return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(READING_TICK_MS)
                viewModel.addReadingSeconds(date, (READING_TICK_MS / 1_000).toInt())
            }
        }
    }
}

@Composable
private fun VersePager(
    state: ReaderState,
    modes: Set<String>,
    bookmarks: Set<VerseRef>,
    playback: com.farukzahra.muslimplus.quran.audio.QuranAudioState,
    palette: ReaderPalette,
    scale: Float,
    onVerseVisible: (Verse) -> Unit,
    onToggleBookmark: (Verse) -> Unit,
    onToggleAudio: (Verse) -> Unit,
    layer: (String, Verse) -> String?,
    modifier: Modifier = Modifier,
) {
    if (state.verses.isEmpty()) return
    val pager = rememberPagerState(initialPage = state.startIndex ?: 0) {
        state.verses.size
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager, state.verses) {
        snapshotFlow { pager.currentPage }
            .distinctUntilChanged()
            .collect { index -> state.verses.getOrNull(index)?.let(onVerseVisible) }
    }
    Column(modifier = modifier.testTag("reader_pager")) {
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val verse = state.verses[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("reader_page_$page"),
            ) {
                VerseBlock(
                    verse = verse,
                    modes = modes,
                    bookmarked = VerseRef(verse.surah, verse.ayah) in bookmarks,
                    playing = playback.surah == verse.surah && playback.ayah == verse.ayah,
                    anyPlaying = playback.playing,
                    palette = palette,
                    scale = scale,
                    onToggleBookmark = { onToggleBookmark(verse) },
                    onToggleAudio = { onToggleAudio(verse) },
                    layer = { layerMode -> layer(layerMode, verse) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { scope.launch { pager.animateScrollToPage((pager.currentPage - 1).coerceAtLeast(0)) } },
                enabled = pager.currentPage > 0,
                modifier = Modifier.testTag("reader_page_previous"),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.action_back),
                    tint = palette.text,
                )
            }
            Text(
                text = stringResource(
                    R.string.quran_page_of,
                    pager.currentPage + 1,
                    state.verses.size,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = palette.muted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = {
                    scope.launch {
                        pager.animateScrollToPage(
                            (pager.currentPage + 1).coerceAtMost(state.verses.lastIndex),
                        )
                    }
                },
                enabled = pager.currentPage < state.verses.lastIndex,
                modifier = Modifier.testTag("reader_page_next"),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.quran_audio_next),
                    tint = palette.text,
                )
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    playback: com.farukzahra.muslimplus.quran.audio.QuranAudioState,
    surahName: String,
    reciterName: String,
    palette: ReaderPalette,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onSpeed: () -> Unit,
) {
    Surface(
        color = palette.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mini_player"),
    ) {
        Column {
            HorizontalDivider(color = palette.outline.copy(alpha = 0.6f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = playback.hasPrevious,
                    modifier = Modifier.testTag("mini_player_previous"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.quran_audio_previous),
                        tint = if (playback.hasPrevious) palette.text else palette.muted.copy(alpha = 0.4f),
                    )
                }
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.testTag("mini_player_play"),
                ) {
                    if (playback.playing) {
                        Icon(
                            painter = painterResource(R.drawable.ic_audio_pause),
                            contentDescription = stringResource(R.string.quran_audio_pause),
                            tint = palette.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.quran_audio_play),
                            tint = palette.accent,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                IconButton(
                    onClick = onNext,
                    enabled = playback.hasNext,
                    modifier = Modifier.testTag("mini_player_next"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.quran_audio_next),
                        tint = if (playback.hasNext) palette.text else palette.muted.copy(alpha = 0.4f),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reciterName,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.quran_audio_now_playing,
                            surahName,
                            playback.ayah ?: 0,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onSpeed,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag("mini_player_speed"),
                ) {
                    Text(
                        text = ReaderPrefs.speedLabel(playback.speed),
                        color = palette.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    palette: ReaderPalette,
    theme: ReaderTheme,
    font: ReaderFont,
    mode: ReaderMode,
    keepScreenOn: Boolean,
    modes: Set<String>,
    onTheme: (ReaderTheme) -> Unit,
    onFont: (ReaderFont) -> Unit,
    onMode: (ReaderMode) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onToggleLayer: (String) -> Unit,
    onAudioSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = scheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
                .testTag("reader_sheet"),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.quran_display),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("reader_sheet_close"),
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            }

            SheetLabel(stringResource(R.string.quran_reader_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReaderTheme.entries.forEach { option ->
                    ThemeSwatch(
                        theme = option,
                        selected = option == theme,
                        onClick = { onTheme(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SheetLabel(stringResource(R.string.quran_reader_text))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderFont.entries.forEach { option ->
                    FilterChip(
                        selected = option == font,
                        onClick = { onFont(option) },
                        label = {
                            Text(
                                text = "A",
                                fontSize = (12 * option.scale).sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        modifier = Modifier.testTag("reader_font_${option.id}"),
                    )
                }
            }

            SheetLabel(stringResource(R.string.quran_reader_mode))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderMode.entries.forEach { option ->
                    FilterChip(
                        selected = option == mode,
                        onClick = { onMode(option) },
                        label = {
                            Text(
                                stringResource(
                                    if (option == ReaderMode.LIST) {
                                        R.string.quran_reader_list
                                    } else {
                                        R.string.quran_reader_page
                                    },
                                ),
                            )
                        },
                        modifier = Modifier.testTag("reader_mode_${option.id}"),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quran_reader_keep_screen),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.quran_reader_keep_screen_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOn,
                    modifier = Modifier.testTag("reader_keep_screen"),
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            SheetLabel(stringResource(R.string.quran_reader_layers))
            layerOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleLayer(option.mode) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = option.mode in modes,
                        onCheckedChange = { onToggleLayer(option.mode) },
                    )
                    Text(
                        text = stringResource(option.labelRes),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.quran_keep_one),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            TextButton(
                onClick = onAudioSettings,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .testTag("reader_audio_settings"),
            ) {
                Text(stringResource(R.string.audio_storage_title))
            }
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun ThemeSwatch(
    theme: ReaderTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview = paletteFor(theme)
    val label = when (theme) {
        ReaderTheme.MODERN -> R.string.quran_reader_theme_modern
        ReaderTheme.PAPER -> R.string.quran_reader_theme_paper
        ReaderTheme.DARK -> R.string.quran_reader_theme_dark
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(16.dp),
            )
            .padding(6.dp)
            .testTag("reader_theme_${theme.id}"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(preview.background),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(preview.accent),
            )
        }
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Download da sura no topo: acao, progresso X/N com cancelar, ou "salvo". */
@Composable
private fun DownloadAction(
    state: AudioDownloadState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
) {
    when {
        state.downloading -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.quran_audio_download_progress, state.done, state.total),
                style = MaterialTheme.typography.labelMedium,
            )
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.quran_audio_download_cancel),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        state.saved -> Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.quran_audio_download_saved),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.quran_audio_download_saved),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        else -> TextButton(onClick = onDownload, enabled = !state.busy) {
            Icon(
                painter = painterResource(R.drawable.ic_audio_download),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.quran_audio_download),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun VerseBlock(
    verse: Verse,
    modes: Set<String>,
    bookmarked: Boolean,
    playing: Boolean,
    anyPlaying: Boolean,
    palette: ReaderPalette,
    scale: Float,
    onToggleBookmark: () -> Unit,
    onToggleAudio: () -> Unit,
    layer: (String) -> String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (playing) palette.accentSoft else Color.Transparent)
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(palette.accentSoft)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    text = stringResource(R.string.verse_reference, verse.surah, verse.ayah),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if ("ar" in modes) {
            Text(
                text = verse.text,
                fontSize = (24 * scale).sp,
                lineHeight = (42 * scale).sp,
                textAlign = TextAlign.Justify,
                style = TextStyle(
                    color = palette.text,
                    textDirection = TextDirection.Rtl,
                ),
                modifier = Modifier.padding(top = 8.dp, end = 12.dp),
            )
        }
        if ("translit" in modes) {
            layer("translit")?.let {
                Text(
                    text = it,
                    fontSize = (15 * scale).sp,
                    lineHeight = (24 * scale).sp,
                    fontStyle = FontStyle.Italic,
                    color = palette.muted,
                    modifier = Modifier.padding(top = 8.dp, end = 12.dp),
                )
            }
        }
        listOf("pt-BR", "en").forEach { locale ->
            if (locale in modes) {
                layer(locale)?.let {
                    Text(
                        text = it,
                        fontSize = (16 * scale).sp,
                        lineHeight = (25 * scale).sp,
                        color = palette.text,
                        modifier = Modifier.padding(top = 8.dp, end = 12.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleAudio) {
                if (playing && anyPlaying) {
                    Icon(
                        painter = painterResource(R.drawable.ic_audio_pause),
                        contentDescription = stringResource(R.string.quran_audio_pause),
                        tint = palette.accent,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.quran_audio_play),
                        tint = if (playing) palette.accent else palette.muted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (bookmarked) BookmarkFilled else BookmarkOutline,
                    contentDescription = stringResource(
                        if (bookmarked) R.string.quran_bookmark_remove else R.string.quran_bookmark_add,
                    ),
                    tint = if (bookmarked) palette.accent else palette.muted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private const val READING_TICK_MS = 15_000L
