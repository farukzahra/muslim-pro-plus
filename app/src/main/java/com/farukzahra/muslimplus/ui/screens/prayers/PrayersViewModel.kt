package com.farukzahra.muslimplus.ui.screens.prayers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerMethod
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MonthDay(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
)

data class PrayersUiState(
    val city: City,
    val method: PrayerMethod,
    val month: YearMonth,
    val today: LocalDate,
    val days: List<MonthDay>,
)

class PrayersViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = ServiceLocator.settings
    private val _state = MutableStateFlow<PrayersUiState?>(null)
    val state: StateFlow<PrayersUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settings.cityId, settings.methodName) { city, method -> city to method }
                .collect { (cityId, methodName) ->
                    _state.value = build(cityId, methodName)
                }
        }
    }

    fun selectCity(id: String) {
        viewModelScope.launch { settings.setCity(id) }
    }

    fun selectMethod(name: String) {
        viewModelScope.launch { settings.setMethod(name) }
    }

    private fun build(cityId: String, methodName: String): PrayersUiState {
        val city = CityCatalog.byId(cityId)
        val method = runCatching { PrayerMethod.valueOf(methodName) }.getOrDefault(PrayerMethod.MWL)
        val zone = ZoneId.of(city.zoneId)
        val today = LocalDate.now(zone)
        val month = YearMonth.from(today)
        val days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val schedule = PrayerEngine.schedule(date, city.latitude, city.longitude, zone, method)
            MonthDay(
                date = date,
                times = schedule.times.mapValues { (_, instant) -> instant.atZone(zone).toLocalTime() },
            )
        }
        return PrayersUiState(city, method, month, today, days)
    }
}
