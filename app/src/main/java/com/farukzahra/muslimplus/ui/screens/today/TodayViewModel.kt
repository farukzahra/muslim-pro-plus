package com.farukzahra.muslimplus.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.location.DefaultLocations
import com.farukzahra.muslimplus.location.GeoLocation
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerSchedule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TodayUiState(
    val location: GeoLocation,
    val date: LocalDate,
    val schedule: PrayerSchedule,
    val next: Prayer?,
    val nextAt: Instant?,
    val tomorrowFajr: Instant?,
    val now: Instant,
    val zone: ZoneId,
)

class TodayViewModel : ViewModel() {
    private val location = DefaultLocations.SaoPaulo

    private val _state = MutableStateFlow(buildState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _state.value = buildState()
                delay(1_000)
            }
        }
    }

    private fun buildState(): TodayUiState {
        val now = Instant.now()
        val zone = location.zone
        val today = LocalDate.now(zone)
        val schedule = PrayerEngine.schedule(today, location.latitude, location.longitude, zone)
        val next = schedule.nextAfter(now)
        val tomorrow = PrayerEngine.schedule(
            today.plusDays(1), location.latitude, location.longitude, zone,
        )
        return TodayUiState(
            location = location,
            date = today,
            schedule = schedule,
            next = next,
            nextAt = next?.let { schedule.timeOf(it) },
            tomorrowFajr = tomorrow.timeOf(Prayer.FAJR),
            now = now,
            zone = zone,
        )
    }
}
