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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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

@OptIn(FlowPreview::class)
class QuranReaderViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val surahId: Int = savedStateHandle.get<Int>("surah") ?: 1
    private val repository = ServiceLocator.quran
    private val settings = ServiceLocator.settings
    private val readerState = ServiceLocator.readerState

    private val _state = MutableStateFlow<ReaderState?>(null)
    val state: StateFlow<ReaderState?> = _state.asStateFlow()

    val modes: StateFlow<Set<String>> = settings.quranModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), setOf("ar"))

    val bookmarks: StateFlow<Set<VerseRef>> = readerState.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Ultimo verso que ficou no topo da tela; o debounce evita gravar a cada quadro. */
    private val firstVisible = MutableStateFlow<Verse?>(null)

    init {
        viewModelScope.launch {
            _state.value = withContext(Dispatchers.IO) {
                val surah = repository.surah(surahId)
                val verses = repository.versesOf(surahId)
                val lastRead = readerState.lastRead.first()
                val ayah = surah?.let { ContinueReading.startAyah(lastRead, it) }
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
    }

    fun onVerseVisible(verse: Verse) {
        firstVisible.value = verse
    }

    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch { readerState.toggleBookmark(verse.surah, verse.ayah) }
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

    private companion object {
        const val LAST_READ_DEBOUNCE_MS = 1_000L
    }
}
