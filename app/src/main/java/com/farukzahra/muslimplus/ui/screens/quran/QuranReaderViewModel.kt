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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReaderState(val surah: Surah?, val verses: List<Verse>)

class QuranReaderViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val surahId: Int = savedStateHandle.get<Int>("surah") ?: 1
    private val repository = ServiceLocator.quran

    private val _state = MutableStateFlow<ReaderState?>(null)
    val state: StateFlow<ReaderState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = withContext(Dispatchers.IO) {
                ReaderState(repository.surah(surahId), repository.versesOf(surahId))
            }
        }
    }
}
