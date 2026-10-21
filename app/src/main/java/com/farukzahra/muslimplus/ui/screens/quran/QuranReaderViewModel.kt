package com.farukzahra.muslimplus.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReaderState(val surah: Surah?, val verses: List<Verse>)

class QuranReaderViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val surahId: Int = savedStateHandle.get<Int>("surah") ?: 1
    private val repository = ServiceLocator.quran
    private val settings = ServiceLocator.settings

    private val _state = MutableStateFlow<ReaderState?>(null)
    val state: StateFlow<ReaderState?> = _state.asStateFlow()

    val modes: StateFlow<Set<String>> = settings.quranModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), setOf("ar"))

    init {
        viewModelScope.launch {
            _state.value = withContext(Dispatchers.IO) {
                ReaderState(repository.surah(surahId), repository.versesOf(surahId))
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settings.quranModes.collect { selected -> selected.forEach(repository::preload) }
        }
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
}
