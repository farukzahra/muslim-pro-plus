package com.farukzahra.muslimplus.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.location.CityCatalog
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TodayUiState(
    val city: City,
    val date: LocalDate,
    val schedule: PrayerSchedule,
    val next: Prayer?,
    val nextAt: Instant?,
    val tomorrowFajr: Instant?,
    val now: Instant,
    val zone: ZoneId,
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = ServiceLocator.settings
    private val _state = MutableStateFlow<TodayUiState?>(null)
    val state: StateFlow<TodayUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settings.cityId, settings.methodName) { city, method -> city to method }
                .collect { (cityId, methodName) ->
                    val city = CityCatalog.byId(cityId)
                    val method = runCatching { PrayerMethod.valueOf(methodName) }
                        .getOrDefault(PrayerMethod.MWL)
                    while (true) {
                        _state.value = build(city, method)
                        delay(1_000)
                    }
                }
        }
    }

    private fun build(city: City, method: PrayerMethod): TodayUiState {
        val now = Instant.now()
        val zone = ZoneId.of(city.zoneId)
        val today = LocalDate.now(zone)
        val schedule = PrayerEngine.schedule(
            today, city.latitude, city.longitude, zone, method,
        )
        val next = schedule.nextAfter(now)
        val tomorrow = PrayerEngine.schedule(
            today.plusDays(1), city.latitude, city.longitude, zone, method,
        )
        return TodayUiState(
            city = city,
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
