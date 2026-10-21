package com.farukzahra.muslimplus.ui.screens.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.ui.components.BookmarkFilled

@Composable
fun QuranListScreen(
    onOpenSurah: (Int) -> Unit,
    onOpenVerse: (Int, Int) -> Unit,
    viewModel: QuranViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val continueRef by viewModel.continueRef.collectAsState()
    val bookmarkCounts by viewModel.bookmarkCounts.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    // O texto sobrevive a rotacao/recriacao; o ViewModel precisa da mesma consulta.
    LaunchedEffect(Unit) { viewModel.setQuery(query) }

    // Nome da sura para rotular cada verso encontrado (o filtro nao muda o catalogo).
    val surahNames = remember(catalog) { catalog.associate { it.id to it.nameLatin } }

    // O card so aparece com a ultima leitura resolvida no catalogo carregado.
    val ref = continueRef
    val continueSurah = ref?.let { last -> catalog.firstOrNull { it.id == last.surah } }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.setQuery(it)
            },
            label = { Text(stringResource(R.string.quran_search)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Durante a busca o card sai da frente do resultado.
            if (state.isEmptyQuery && ref != null && continueSurah != null) {
                item(key = "continue") {
                    ContinueCard(
                        surahName = continueSurah.nameLatin,
                        ayah = ref.ayah,
                        onClick = { onOpenSurah(continueSurah.id) },
                    )
                }
            }
            if (!state.isEmptyQuery && state.surahMatches.isNotEmpty()) {
                item(key = "header-surahs") {
                    SectionHeader(stringResource(R.string.quran_search_suras))
                }
            }
            items(state.surahMatches, key = { "surah-${it.id}" }) { surah ->
                ListItem(
                    headlineContent = { Text(surah.nameLatin) },
                    supportingContent = {
                        Text(
                            text = "${surah.id} · " +
                                stringResource(R.string.quran_verses, surah.verseCount),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BookmarkCount(count = bookmarkCounts[surah.id] ?: 0)
                            Text(
                                text = surah.nameArabic,
                                fontSize = 22.sp,
                                textAlign = TextAlign.End,
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSurah(surah.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }
            if (state.searchesVerses) {
                item(key = "header-verses") {
                    SectionHeader(stringResource(R.string.quran_search_verses))
                }
                if (state.verseMatches.isEmpty() && !state.versesPending) {
                    item(key = "verses-empty") {
                        Text(
                            text = stringResource(R.string.quran_search_no_verses),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
                items(
                    items = state.verseMatches,
                    key = { verse -> "verse-${verse.surah}:${verse.ayah}" },
                ) { verse ->
                    VerseResult(
                        verse = verse,
                        surahName = surahNames[verse.surah].orEmpty(),
                        onClick = { onOpenVerse(verse.surah, verse.ayah) },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun VerseResult(verse: Verse, surahName: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = surahName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.verse_reference, verse.surah, verse.ayah),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = verse.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 19.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Start,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                textDirection = TextDirection.Rtl,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Composable
private fun ContinueCard(surahName: String, ayah: Int, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = scheme.primaryContainer.copy(alpha = 0.45f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(R.string.quran_continue_title),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
            )
            Text(
                text = stringResource(R.string.quran_continue_position, surahName, ayah),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun BookmarkCount(count: Int) {
    if (count <= 0) return
    val description = stringResource(R.string.quran_bookmarks_count, count)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Icon(
            imageVector = BookmarkFilled,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
    }
}
