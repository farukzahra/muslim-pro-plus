package com.farukzahra.muslimplus.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.ContinueReading
import com.farukzahra.muslimplus.quran.SearchResults
import com.farukzahra.muslimplus.quran.SearchUiState
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.VerseRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Resultado de uma consulta; o termo junto evita pintar versos de outra busca. */
private data class VerseHits(val query: String, val verses: List<Verse>)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ServiceLocator.quran
    private val readerState = ServiceLocator.readerState
    private val surahs = MutableStateFlow<List<Surah>>(emptyList())
    private val query = MutableStateFlow("")

    /** Catalogo completo (sem filtro), para rotular os versos encontrados. */
    val catalog: StateFlow<List<Surah>> = surahs.asStateFlow()

    /**
     * Busca de versos com debounce de [SEARCH_DEBOUNCE_MS]: cada tecla reinicia
     * o timer e [mapLatest] cancela a consulta anterior. A varredura dos 6.236
     * versos sai da main em [Dispatchers.IO].
     */
    private val verseHits: StateFlow<VerseHits> = query
        .debounce(SEARCH_DEBOUNCE_MS)
        .mapLatest { term -> searchVerses(term) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            VerseHits(query = "", verses = emptyList()),
        )

    /** As duas secoes da lista: suras filtradas na hora, versos com debounce. */
    val state: StateFlow<SearchUiState> = combine(query, surahs, verseHits) { term, list, hits ->
        // Enquanto o debounce nao devolve a consulta nova, a secao de versos fica vazia.
        val fresh = hits.query == term.trim()
        SearchResults.split(term, list, hits.verses.takeIf { fresh }.orEmpty())
            .copy(versesPending = SearchResults.isVerseQuery(term) && !fresh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    /** Ultima leitura valida no catalogo; `null` esconde o card. */
    val continueRef: StateFlow<VerseRef?> =
        combine(readerState.lastRead, surahs) { lastRead, list ->
            ContinueReading.resolve(lastRead, list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Marcadores por sura, para o indicador discreto na lista. */
    val bookmarkCounts: StateFlow<Map<Int, Int>> = readerState.bookmarks
        .map { refs -> refs.groupingBy { it.surah }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        viewModelScope.launch {
            surahs.value = withContext(Dispatchers.IO) { repository.surahs() }
        }
    }

    private suspend fun searchVerses(term: String): VerseHits {
        val clean = term.trim()
        if (!SearchResults.isVerseQuery(clean)) return VerseHits(clean, emptyList())
        val hits = withContext(Dispatchers.IO) {
            repository.searchVerses(clean, SearchResults.MAX_VERSES)
        }
        return VerseHits(clean, hits)
    }

    fun setQuery(value: String) {
        query.value = value
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
