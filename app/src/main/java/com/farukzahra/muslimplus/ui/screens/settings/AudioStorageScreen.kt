package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.audio.QuranAudioLibrary
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import com.farukzahra.muslimplus.quran.audio.QuranReciter
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import com.farukzahra.muslimplus.quran.audio.QuranStreamingCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Áudio do Alcorão em três blocos, na ordem em que a pessoa decide: quem canta,
 * o que já está baixado e quanto ocupa. O recitador fica no topo com um botão
 * próprio; antes ele era só mais uma linha numa lista de 114 suratas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStorageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val library = remember { QuranAudioLibrary.get(context) }
    val scope = rememberCoroutineScope()
    val selectedId by ServiceLocator.settings.quranReciterId.collectAsState(QuranReciters.default.id)
    val selected = QuranReciters.find(selectedId)
    val progress by library.progress.collectAsState()
    val revision by library.revision.collectAsState()
    val surahs = remember { ServiceLocator.quran.surahs() }
    var query by remember { mutableStateOf("") }
    var downloadedOnly by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf(false) }
    var inventory by remember { mutableStateOf<Map<String, Map<Int, Int>>>(emptyMap()) }
    var sizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var cacheBytes by remember { mutableLongStateOf(0L) }
    var refresh by remember { mutableIntStateOf(0) }
    var confirmAll by remember { mutableStateOf<QuranReciter?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<QuranReciter, Int?>?>(null) }
    var insufficient by remember { mutableStateOf(false) }
    var storageFailed by remember { mutableStateOf(false) }
    var cleaning by remember { mutableStateOf(false) }

    LaunchedEffect(revision, refresh) {
        withContext(Dispatchers.IO) {
            inventory = QuranReciters.all.associate { voice ->
                val store = QuranAudioStore(context, voice)
                voice.id to surahs.associate { it.id to store.savedVerses(it.id, it.verseCount) }
            }
            sizes = QuranReciters.all.associate { it.id to QuranAudioStore(context, it).totalBytes() }
            cacheBytes = QuranStreamingCache.bytes(context)
        }
    }

    val savedSurahs = selected?.let { voice ->
        surahs.count { (inventory[voice.id]?.get(it.id) ?: 0) > 0 }
    } ?: 0
    val savedBytes = selected?.let { sizes[it.id] ?: 0L } ?: 0L

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.audio_storage_title)) }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
            }
        })
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionTitle(stringResource(R.string.audio_reciter_section)) }
            item {
                SelectedReciterCard(
                    voice = selected,
                    bytes = savedBytes,
                    onChange = { picker = true },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle(stringResource(R.string.audio_space_section))
            }
            item {
                Text(
                    text = stringResource(
                        R.string.audio_space_usage,
                        formatBytes(context, sizes.values.sum()),
                        formatBytes(context, cacheBytes),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(enabled = !cleaning, onClick = {
                    cleaning = true
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { QuranStreamingCache.clear(context) }
                            library.refresh()
                            refresh++
                        } catch (_: java.io.IOException) {
                            storageFailed = true
                        } finally {
                            cleaning = false
                        }
                    }
                }, modifier = Modifier.testTag("clear_audio_cache")) {
                    Text(stringResource(R.string.audio_clear_cache))
                }
                Text(
                    text = stringResource(R.string.audio_arabic_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle(stringResource(R.string.audio_downloads_section))
            }
            item {
                Text(
                    text = stringResource(
                        R.string.audio_saved_summary,
                        savedSurahs,
                        surahs.size,
                        formatBytes(context, savedBytes),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.audio_lifecycle_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Button(
                        enabled = !progress.running && selected != null,
                        onClick = { selected?.let { confirmAll = it } },
                    ) {
                        Text(stringResource(R.string.audio_download_all))
                    }
                    TextButton(
                        enabled = !progress.running && savedBytes > 0,
                        onClick = { selected?.let { deleteTarget = it to null } },
                    ) {
                        Text(stringResource(R.string.audio_delete_voice))
                    }
                }
            }
            item {
                if (progress.running) {
                    Text(
                        stringResource(
                            R.string.audio_progress,
                            QuranReciters.find(progress.reciterId ?: "")?.reciter ?: "",
                            progress.done,
                            progress.total,
                        ),
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (progress.total == 0) 0f else progress.done.toFloat() / progress.total
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = library::cancel) { Text(stringResource(R.string.audio_cancel)) }
                }
                if (progress.failed) {
                    Text(
                        stringResource(R.string.audio_download_failed),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.audio_search)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(downloadedOnly, { downloadedOnly = it })
                    Text(stringResource(R.string.audio_downloaded_only))
                }
            }

            val visible = surahs.filter { surah ->
                surah.nameLatin.contains(query, true) &&
                    (!downloadedOnly || (selected?.let { inventory[it.id]?.get(surah.id) ?: 0 } ?: 0) > 0)
            }
            items(visible, key = { "surah_${it.id}" }) { surah ->
                val count = selected?.let { inventory[it.id]?.get(surah.id) ?: 0 } ?: 0
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.audio_surah_title, surah.id, surah.nameLatin))
                    },
                    supportingContent = {
                        Text(
                            stringResource(
                                if (count == surah.verseCount) R.string.audio_complete else R.string.audio_partial,
                                count,
                                surah.verseCount,
                            ),
                        )
                    },
                )
                Row {
                    TextButton(
                        enabled = !progress.running && selected != null,
                        onClick = { selected?.let { library.download(it, listOf(surah)) } },
                    ) {
                        Text(stringResource(R.string.audio_download))
                    }
                    TextButton(
                        enabled = !progress.running && count > 0,
                        onClick = { selected?.let { deleteTarget = it to surah.id } },
                    ) {
                        Text(stringResource(R.string.audio_delete))
                    }
                }
                HorizontalDivider()
            }
        }
    }

    if (picker) {
        ReciterPickerSheet(
            selectedId = selectedId,
            sizes = sizes,
            onPick = { voice ->
                picker = false
                scope.launch { ServiceLocator.settings.setQuranReciterId(voice.id) }
            },
            onDismiss = { picker = false },
        )
    }

    confirmAll?.let { voice ->
        AlertDialog(
            onDismissRequest = { confirmAll = null },
            title = { Text(stringResource(R.string.audio_download_all)) },
            text = {
                Text(
                    stringResource(
                        R.string.audio_estimate,
                        formatBytes(context, QuranAudioLibrary.estimatedBytes(voice)),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmAll = null
                    scope.launch {
                        val enough = withContext(Dispatchers.IO) {
                            val store = QuranAudioStore(context, voice)
                            val remaining = surahs.sumOf {
                                it.verseCount - store.savedVerses(it.id, it.verseCount)
                            }
                            context.filesDir.usableSpace >=
                                QuranAudioLibrary.requiredBytes(voice, remaining)
                        }
                        if (!enough) insufficient = true else library.download(voice, surahs)
                    }
                }) { Text(stringResource(R.string.audio_download)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmAll = null }) {
                    Text(stringResource(R.string.audio_cancel))
                }
            },
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.audio_delete)) },
            text = { Text(stringResource(R.string.audio_delete_confirm)) },
            confirmButton = {
                TextButton(enabled = !progress.running, onClick = {
                    deleteTarget = null
                    scope.launch { storageFailed = !library.delete(target.first, target.second) }
                }) { Text(stringResource(R.string.audio_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.audio_cancel))
                }
            },
        )
    }
    if (storageFailed) {
        AlertDialog(
            onDismissRequest = { storageFailed = false },
            title = { Text(stringResource(R.string.audio_storage_failed)) },
            confirmButton = {
                TextButton(onClick = { storageFailed = false }) {
                    Text(stringResource(R.string.audio_cancel))
                }
            },
        )
    }
    if (insufficient) {
        AlertDialog(
            onDismissRequest = { insufficient = false },
            title = { Text(stringResource(R.string.audio_no_space)) },
            confirmButton = {
                TextButton(onClick = { insufficient = false }) {
                    Text(stringResource(R.string.audio_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SelectedReciterCard(
    voice: QuranReciter?,
    bytes: Long,
    onChange: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voice?.reciter ?: stringResource(R.string.audio_unknown_voice),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = voice?.let {
                        stringResource(
                            R.string.audio_voice_quality,
                            it.bitrateKbps,
                            formatBytes(context, bytes),
                        )
                    }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onChange, modifier = Modifier.testTag("reciter_change")) {
                Text(stringResource(R.string.audio_change_voice))
            }
        }
    }
}
