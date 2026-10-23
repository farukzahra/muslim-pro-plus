package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.audio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.audio_storage_title)) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back)) }
        })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                Text(stringResource(R.string.audio_arabic_hint), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.audio_lifecycle_hint), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.audio_download_size, formatBytes(context, sizes.values.sum())))
                Text(stringResource(R.string.audio_cache_size, formatBytes(context, cacheBytes)))
                TextButton(enabled = !cleaning, modifier = Modifier.testTag("clear_audio_cache"), onClick = {
                    cleaning = true
                    scope.launch { try { withContext(Dispatchers.IO) { QuranStreamingCache.clear(context) }; library.refresh(); refresh++ } catch (_: java.io.IOException) { storageFailed = true } finally { cleaning = false } }
                }) { Text(stringResource(R.string.audio_clear_cache)) }
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(stringResource(R.string.audio_search)) }, modifier = Modifier.fillMaxWidth())
                Row { Checkbox(downloadedOnly, { downloadedOnly = it }); Text(stringResource(R.string.audio_downloaded_only), Modifier.padding(top = 12.dp)) }
                if (selected == null) Text(stringResource(R.string.audio_unknown_voice), color = MaterialTheme.colorScheme.error)
            }
            items(QuranReciters.all.filter { it.reciter.contains(query, true) && (!downloadedOnly || (sizes[it.id] ?: 0) > 0) }, key = { it.id }) { voice ->
                ListItem(headlineContent = { Text(voice.reciter) }, supportingContent = { Text(stringResource(R.string.audio_voice_quality, voice.bitrateKbps, formatBytes(context, sizes[voice.id] ?: 0))) }, leadingContent = { RadioButton(selectedId == voice.id, onClick = null) }, modifier = Modifier.testTag("reciter_${voice.id}").clickable { scope.launch { ServiceLocator.settings.setQuranReciterId(voice.id) } })
            }
            item {
                HorizontalDivider()
                if (progress.running) {
                    Text(stringResource(R.string.audio_progress, QuranReciters.find(progress.reciterId ?: "")?.reciter ?: "", progress.done, progress.total))
                    LinearProgressIndicator(progress = { if (progress.total == 0) 0f else progress.done.toFloat() / progress.total }, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = library::cancel) { Text(stringResource(R.string.audio_cancel)) }
                }
                if (progress.failed) Text(stringResource(R.string.audio_download_failed), color = MaterialTheme.colorScheme.error)
                selected?.let { voice ->
                    Text(voice.reciter, style = MaterialTheme.typography.titleMedium)
                    Button(enabled = !progress.running, onClick = { confirmAll = voice }) { Text(stringResource(R.string.audio_download_all)) }
                    TextButton(enabled = !progress.running && (sizes[voice.id] ?: 0) > 0, onClick = { deleteTarget = voice to null }) { Text(stringResource(R.string.audio_delete_voice)) }
                }
            }
            if (selected != null) items(surahs, key = { "surah_${it.id}" }) { surah ->
                val count = inventory[selected.id]?.get(surah.id) ?: 0
                ListItem(headlineContent = { Text(stringResource(R.string.audio_surah_title, surah.id, surah.nameLatin)) }, supportingContent = { Text(stringResource(if (count == surah.verseCount) R.string.audio_complete else R.string.audio_partial, count, surah.verseCount)) })
                Row {
                    TextButton(enabled = !progress.running, onClick = { library.download(selected, listOf(surah)) }) { Text(stringResource(R.string.audio_download)) }
                    TextButton(enabled = !progress.running && count > 0, onClick = { deleteTarget = selected to surah.id }) { Text(stringResource(R.string.audio_delete)) }
                }
                HorizontalDivider()
            }
        }
    }
    confirmAll?.let { voice ->
        AlertDialog(onDismissRequest = { confirmAll = null }, title = { Text(stringResource(R.string.audio_download_all)) }, text = { Text(stringResource(R.string.audio_estimate, formatBytes(context, QuranAudioLibrary.estimatedBytes(voice)))) }, confirmButton = {
            TextButton(onClick = {
                confirmAll = null
                scope.launch {
                    val enough = withContext(Dispatchers.IO) {
                        val store = QuranAudioStore(context, voice)
                        val remaining = surahs.sumOf { it.verseCount - store.savedVerses(it.id, it.verseCount) }
                        context.filesDir.usableSpace >= QuranAudioLibrary.requiredBytes(voice, remaining)
                    }
                    if (!enough) insufficient = true else library.download(voice, surahs)
                }
            }) { Text(stringResource(R.string.audio_download)) }
        }, dismissButton = { TextButton(onClick = { confirmAll = null }) { Text(stringResource(R.string.audio_cancel)) } })
    }
    deleteTarget?.let { target ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text(stringResource(R.string.audio_delete)) }, text = { Text(stringResource(R.string.audio_delete_confirm)) }, confirmButton = {
            TextButton(enabled = !progress.running, onClick = { deleteTarget = null; scope.launch { storageFailed = !library.delete(target.first, target.second) } }) { Text(stringResource(R.string.audio_delete)) }
        }, dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.audio_cancel)) } })
    }
    if (storageFailed) AlertDialog(onDismissRequest = { storageFailed = false }, title = { Text(stringResource(R.string.audio_storage_failed)) }, confirmButton = { TextButton(onClick = { storageFailed = false }) { Text(stringResource(R.string.audio_cancel)) } })
    if (insufficient) AlertDialog(onDismissRequest = { insufficient = false }, title = { Text(stringResource(R.string.audio_no_space)) }, confirmButton = { TextButton(onClick = { insufficient = false }) { Text(stringResource(R.string.audio_cancel)) } })
}

private fun formatBytes(context: android.content.Context, bytes: Long): String = android.text.format.Formatter.formatShortFileSize(context, bytes)
