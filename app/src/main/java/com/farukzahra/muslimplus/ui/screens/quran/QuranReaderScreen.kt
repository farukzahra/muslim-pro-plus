package com.farukzahra.muslimplus.ui.screens.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef
import com.farukzahra.muslimplus.ui.components.BookmarkFilled
import com.farukzahra.muslimplus.ui.components.BookmarkOutline
import kotlinx.coroutines.flow.distinctUntilChanged

private data class LayerOption(val mode: String, val labelRes: Int)

private val layerOptions = listOf(
    LayerOption("ar", R.string.quran_arabic),
    LayerOption("translit", R.string.quran_transliteration),
    LayerOption("pt-BR", R.string.language_portuguese),
    LayerOption("en", R.string.language_english),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    onBack: () -> Unit,
    viewModel: QuranReaderViewModel = viewModel(),
) {
    val state = viewModel.state.collectAsState().value ?: return
    val modes by viewModel.modes.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Restaura a posicao uma unica vez: depois disso, rolar e do usuario.
    LaunchedEffect(state.startIndex) {
        state.startIndex?.let { if (it > 0) listState.scrollToItem(it) }
    }

    // Primeiro verso visivel -> ultima leitura (o ViewModel aplica o debounce).
    LaunchedEffect(state.verses) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index -> state.verses.getOrNull(index)?.let(viewModel::onVerseVisible) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.surah?.nameLatin.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.quran_display),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
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
                    onToggleBookmark = { viewModel.toggleBookmark(verse) },
                    layer = { mode -> viewModel.layer(mode, verse) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text(stringResource(R.string.quran_display)) },
            text = {
                Column {
                    layerOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggle(option.mode) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = option.mode in modes,
                                onCheckedChange = { viewModel.toggle(option.mode) },
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptions = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }
}

@Composable
private fun VerseBlock(
    verse: Verse,
    modes: Set<String>,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    layer: (String) -> String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 6.dp),
    ) {
        if ("ar" in modes) {
            Text(
                text = verse.text,
                fontSize = 24.sp,
                lineHeight = 42.sp,
                textAlign = TextAlign.Justify,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textDirection = TextDirection.Rtl,
                ),
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        if ("translit" in modes) {
            layer("translit")?.let {
                Text(
                    text = it,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, end = 12.dp),
                )
            }
        }
        listOf("pt-BR", "en").forEach { locale ->
            if (locale in modes) {
                layer(locale)?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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
            Text(
                text = stringResource(R.string.verse_reference, verse.surah, verse.ayah),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (bookmarked) BookmarkFilled else BookmarkOutline,
                    contentDescription = stringResource(
                        if (bookmarked) R.string.quran_bookmark_remove else R.string.quran_bookmark_add,
                    ),
                    tint = if (bookmarked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
