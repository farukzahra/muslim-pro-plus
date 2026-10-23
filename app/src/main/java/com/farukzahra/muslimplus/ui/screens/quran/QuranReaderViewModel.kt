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
import com.farukzahra.muslimplus.quran.audio.QuranAudioLibrary
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import com.farukzahra.muslimplus.quran.audio.QuranAudioItems
import com.farukzahra.muslimplus.quran.audio.QuranAudioState
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    /** Existe fila de download em andamento, mesmo que de outra sura. */
    val busy: Boolean = false,
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
    private val library = QuranAudioLibrary.get(application)
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

    /** Evento para pedir escolha de voz quando a preferência salva não existe mais. */
    private val _audioPrompt = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val audioPrompt: SharedFlow<Unit> = _audioPrompt.asSharedFlow()

    val reciter = settings.quranReciterId.map { QuranReciters.find(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, QuranReciters.default)

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
        // A preferência afeta a próxima reprodução; nunca muda a voz no meio do verso.
        viewModelScope.launch {
            var previousId: String? = null
            settings.quranReciterId.distinctUntilChanged().collect { id ->
                if ((previousId != null && previousId != id) ||
                    (audio.state.value.reciterId != null && audio.state.value.reciterId != id)) audio.pause()
                previousId = id
            }
        }
        viewModelScope.launch {
            combine(settings.quranReciterId, library.revision, _state) { id, _, reader -> id to reader }
                .collect { (id, reader) ->
                    val surah = reader?.surah ?: return@collect
                    val source = QuranReciters.find(id)
                    val saved = source != null && withContext(Dispatchers.IO) {
                        QuranAudioStore(application, source).isSurahSaved(surah.id, reader.verses.size)
                    }
                    _download.update { it.copy(saved = saved, total = reader.verses.size) }
                }
        }
        // Progresso vem a cada verso; não recalcula o disco para não competir com o download.
        viewModelScope.launch {
            library.progress.collect { progress ->
                val reader = _state.value
                val surah = reader?.surah
                val matches = surah != null &&
                    progress.reciterId == reciter.value?.id && progress.surah == surah.id
                _download.update {
                    it.copy(
                        downloading = progress.running && matches,
                        done = if (matches) progress.surahDone else 0,
                        total = reader?.verses?.size ?: it.total,
                        busy = progress.running,
                        failed = matches && progress.failed,
                    )
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
        val source = reciter.value ?: run {
            _audioPrompt.tryEmit(Unit)
            return
        }
        val current = audio.state.value
        if (current.surah == verse.surah && current.ayah == verse.ayah && current.reciterId == source.id) {
            if (current.playing) audio.pause() else audio.resume()
            return
        }
        val index = reader.verses.indexOfFirst { it.ayah == verse.ayah }.coerceAtLeast(0)
        val items = QuranAudioItems.forSurah(getApplication(), surah, reader.verses, source)
        audio.play(items, index)
    }

    fun downloadAudio() {
        val reader = _state.value ?: return
        val surah = reader.surah ?: return
        val source = reciter.value ?: run {
            _audioPrompt.tryEmit(Unit)
            return
        }
        if (library.progress.value.running) return
        library.download(source, listOf(surah))
    }

    fun cancelDownload() {
        if (library.progress.value.reciterId == reciter.value?.id) library.cancel()
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
        audio.release()
        super.onCleared()
    }

    private companion object {
        const val LAST_READ_DEBOUNCE_MS = 1_000L
    }
}
