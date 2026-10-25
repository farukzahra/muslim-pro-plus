package com.farukzahra.muslimplus.ui.update

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.update.AppUpdater
import com.farukzahra.muslimplus.update.UpdateAvailability
import com.farukzahra.muslimplus.update.UpdateCardState
import com.farukzahra.muslimplus.update.UpdateDialogKind
import com.farukzahra.muslimplus.update.UpdatePromptPolicy
import com.farukzahra.muslimplus.update.UpdatePromptStore
import com.farukzahra.muslimplus.update.UpdateSignals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UpdateUiState(
    val dialog: UpdateDialogKind? = null,
    val card: UpdateCardState = UpdateCardState.Unknown,
    val downloadPercent: Int? = null,
)

class UpdateViewModel(
    private val updater: AppUpdater,
    private val prompts: UpdatePromptStore,
) : ViewModel() {
    private val signals = MutableStateFlow(UpdateSignals())
    private val restartPostponed = MutableStateFlow(false)

    val uiState: StateFlow<UpdateUiState> = combine(
        signals,
        prompts.observePromptDismissed(),
        restartPostponed,
    ) { current, dismissed, postponed ->
        UpdateUiState(
            dialog = UpdatePromptPolicy.dialogFor(current, dismissed, postponed),
            card = UpdatePromptPolicy.cardFor(current),
            downloadPercent = UpdatePromptPolicy.downloadPercent(current),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateUiState(),
    )

    init {
        viewModelScope.launch {
            updater.observeInstallState().collect(::applySignals)
        }
        checkNow()
    }

    fun onResume() = checkNow()

    fun checkNow() {
        viewModelScope.launch { applySignals(updater.check()) }
    }

    fun onUpdateClick(activity: Activity) {
        viewModelScope.launch { updater.startFlexibleUpdate(activity) }
    }

    fun onPromptDismissed() {
        viewModelScope.launch { prompts.setPromptDismissed(true) }
    }

    fun onRestartClick() {
        viewModelScope.launch { updater.completeUpdate() }
    }

    fun onRestartPostponed() {
        restartPostponed.value = true
    }

    private suspend fun applySignals(state: UpdateSignals) {
        signals.value = state
        if (state.availability != UpdateAvailability.Downloaded) restartPostponed.value = false
        if (UpdatePromptPolicy.clearsDismissal(state)) prompts.setPromptDismissed(false)
    }
}

class UpdateViewModelFactory(
    private val updater: AppUpdater,
    private val prompts: UpdatePromptStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
            return UpdateViewModel(updater, prompts) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
