package com.farukzahra.muslimplus.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.ContinueReading
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef
import com.farukzahra.muslimplus.quran.audio.QuranAudioController
import com.farukzahra.muslimplus.quran.audio.QuranAudioDownloader
import com.farukzahra.muslimplus.quran.audio.QuranAudioItems
import com.farukzahra.muslimplus.quran.audio.QuranAudioState
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [startIndex] = verso da ultima leitura dentro desta sura, resolvido uma vez
 * na abertura (depois disso o usuario manda na rolagem).
 */
data class ReaderState(
    val surah: Surah?,
    val verses: List<Verse>,
    val startIndex: Int? = null,
)

/** Estado do download da sura aberta, exibido no topo do leitor. */
data class AudioDownloadState(
    val downloading: Boolean = false,
    val done: Int = 0,
    val total: Int = 0,
    val saved: Boolean = false,
    val failed: Boolean = false,
)

@OptIn(FlowPreview::class)
class QuranReaderViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val surahId: Int = savedStateHandle.get<Int>("surah") ?: 1

    /** Verso pedido pela busca (`quran/2?ayah=255`); 0 cai na ultima leitura. */
    private val requestedAyah: Int = savedStateHandle.get<Int>("ayah") ?: 0

    private val repository = ServiceLocator.quran
    private val settings = ServiceLocator.settings
    private val readerState = ServiceLocator.readerState
    private val audioStore = QuranAudioStore(application)
    private val audioDownloader = QuranAudioDownloader(audioStore)
    private val audio = QuranAudioController(application)

    private val _state = MutableStateFlow<ReaderState?>(null)
    val state: StateFlow<ReaderState?> = _state.asStateFlow()

    val modes: StateFlow<Set<String>> = settings.quranModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), setOf("ar"))

    val bookmarks: StateFlow<Set<VerseRef>> = readerState.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val playback: StateFlow<QuranAudioState> = audio.state

    private val _download = MutableStateFlow(AudioDownloadState())
    val download: StateFlow<AudioDownloadState> = _download.asStateFlow()

    private var downloadJob: Job? = null

    /** Ultimo verso que ficou no topo da tela; o debounce evita gravar a cada quadro. */
    private val firstVisible = MutableStateFlow<Verse?>(null)

    init {
        audio.connect()
        viewModelScope.launch {
            _state.value = withContext(Dispatchers.IO) {
                val surah = repository.surah(surahId)
                val verses = repository.versesOf(surahId)
                val lastRead = readerState.lastRead.first()
                // A busca manda no verso inicial; sem ela vale a ultima leitura.
                val ayah = surah?.let { catalog ->
                    requestedAyah.takeIf { it > 0 }
                        ?.let { VerseRef.of(surahId, it) }
                        ?.let { ContinueReading.startAyah(it, catalog) }
                        ?: ContinueReading.startAyah(lastRead, catalog)
                }
                ReaderState(
                    surah = surah,
                    verses = verses,
                    startIndex = ayah?.let { ContinueReading.verseIndex(verses, it) },
                )
            }
            refreshSaved()
        }
        viewModelScope.launch(Dispatchers.IO) {
            settings.quranModes.collect { selected -> selected.forEach(repository::preload) }
        }
        viewModelScope.launch {
            firstVisible
                .filterNotNull()
                .debounce(LAST_READ_DEBOUNCE_MS)
                .collect { verse -> readerState.saveLastRead(verse.surah, verse.ayah) }
        }
        // Cada verso que comeca a tocar vira o mais recente no cache LRU.
        viewModelScope.launch {
            audio.state
                .map { current -> current.surah to current.ayah }
                .distinctUntilChanged()
                .collect { (surah, ayah) ->
                    if (surah != null && ayah != null) {
                        withContext(Dispatchers.IO) { audioStore.touch(surah, ayah) }
                    }
                }
        }
    }

    fun onVerseVisible(verse: Verse) {
        firstVisible.value = verse
    }

    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch { readerState.toggleBookmark(verse.surah, verse.ayah) }
    }

    /**
     * Play/pause do verso. Mesmo verso -> alterna; outro verso -> monta a fila
     * da sura inteira comecando nele (o ExoPlayer avanca sozinho no fim).
     */
    fun onVerseAudio(verse: Verse) {
        val reader = _state.value ?: return
        val surah = reader.surah ?: return
        val current = audio.state.value
        if (current.surah == verse.surah && current.ayah == verse.ayah) {
            if (current.playing) audio.pause() else audio.resume()
            return
        }
        val index = reader.verses.indexOfFirst { it.ayah == verse.ayah }.coerceAtLeast(0)
        val items = QuranAudioItems.forSurah(getApplication(), audioStore, surah, reader.verses)
        audio.play(items, index)
    }

    fun downloadAudio() {
        val reader = _state.value ?: return
        val surah = reader.surah ?: return
        if (_download.value.downloading) return

        val total = reader.verses.size
        _download.value = AudioDownloadState(downloading = true, total = total)
        downloadJob = viewModelScope.launch {
            try {
                val result = audioDownloader.download(surah.id, total) { done, count ->
                    _download.value = _download.value.copy(done = done, total = count)
                }
                val saved = withContext(Dispatchers.IO) {
                    audioStore.isSurahSaved(surah.id, total)
                }
                _download.value = AudioDownloadState(saved = saved, failed = !result.isComplete)
            } catch (cancelled: CancellationException) {
                // cancelDownload() ja deixou o estado limpo.
                throw cancelled
            }
            downloadJob = null
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        val reader = _state.value ?: return
        val surah = reader.surah ?: return
        _download.value = AudioDownloadState()
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                audioStore.isSurahSaved(surah.id, reader.verses.size)
            }
            _download.value = _download.value.copy(saved = saved)
        }
    }

    /** "Baixar audio" so aparece como concluido se a sura inteira esta no disco. */
    private suspend fun refreshSaved() {
        val reader = _state.value ?: return
        val surah = reader.surah ?: return
        val saved = withContext(Dispatchers.IO) {
            audioStore.isSurahSaved(surah.id, reader.verses.size)
        }
        _download.value = _download.value.copy(saved = saved)
    }

    /** Nunca deixa o conjunto vazio: pelo menos uma camada fica ligada. */
    fun toggle(mode: String) {
        viewModelScope.launch {
            val current = settings.quranModes.first()
            val next = if (mode in current) current - mode else current + mode
            if (next.isNotEmpty()) settings.setQuranModes(next)
        }
    }

    fun layer(mode: String, verse: Verse): String? =
        repository.layer(mode, "${verse.surah}:${verse.ayah}")

    override fun onCleared() {
        downloadJob?.cancel()
        audio.release()
        super.onCleared()
    }

    private companion object {
        const val LAST_READ_DEBOUNCE_MS = 1_000L
    }
}
