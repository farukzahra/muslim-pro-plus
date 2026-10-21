package com.farukzahra.muslimplus.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.ContinueReading
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.VerseRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ServiceLocator.quran
    private val readerState = ServiceLocator.readerState
    private val surahs = MutableStateFlow<List<Surah>>(emptyList())
    private val query = MutableStateFlow("")

    val state: StateFlow<List<Surah>> = combine(surahs, query) { list, term ->
        if (term.isBlank()) {
            list
        } else {
            list.filter {
                it.nameLatin.contains(term, ignoreCase = true) ||
                    it.nameArabic.contains(term) ||
                    it.id.toString() == term.trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun setQuery(value: String) {
        query.value = value
    }
}
