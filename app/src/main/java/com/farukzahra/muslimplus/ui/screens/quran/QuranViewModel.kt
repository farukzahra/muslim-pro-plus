package com.farukzahra.muslimplus.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.quran.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ServiceLocator.quran
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

    init {
        viewModelScope.launch {
            surahs.value = withContext(Dispatchers.IO) { repository.surahs() }
        }
    }

    fun setQuery(value: String) {
        query.value = value
    }
}
