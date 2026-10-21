package com.farukzahra.muslimplus.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.notifications.ExactAlarms
import com.farukzahra.muslimplus.notifications.PrayerScheduler
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val exactAlarmsAvailable: Boolean,
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = ServiceLocator.settings
    private val _state = MutableStateFlow<TodayUiState?>(null)
    val state: StateFlow<TodayUiState?> = _state.asStateFlow()

    // Lido uma vez e atualizado no ON_RESUME: canScheduleExactAlarms() consulta
    // o appops, nao precisa rodar a cada segundo do relogio.
    private var exactAlarmsAvailable = ExactAlarms.isAvailable(application)

    /**
     * Chamado quando a tela volta ao primeiro plano, caso o usuario tenha
     * concedido ou revogado a permissao enquanto estava fora.
     */
    fun refreshExactAlarms() {
        val available = ExactAlarms.isAvailable(getApplication())
        if (available == exactAlarmsAvailable) return
        exactAlarmsAvailable = available
        _state.value = _state.value?.copy(exactAlarmsAvailable = available)
        if (available) {
            // O alarme atual foi agendado como inexato; refaz agora que da.
            PrayerScheduler(getApplication()).reschedule()
        }
    }

    init {
        viewModelScope.launch {
            combine(
                settings.cityId,
                settings.methodName,
                settings.prayerOffsets,
            ) { city, method, offsets -> Triple(city, method, offsets) }
                .distinctUntilChanged()
                .collectLatest { (cityId, methodName, offsets) ->
                    val city = CityCatalog.byId(cityId)
                    val method = runCatching { PrayerMethod.valueOf(methodName) }
                        .getOrDefault(PrayerMethod.MWL)
                    // collectLatest cancela este loop quando cidade, metodo ou
                    // offsets mudam, para o relogio nao segurar a config antiga.
                    while (true) {
                        _state.value = build(city, method, offsets)
                        delay(1_000)
                    }
                }
        }
    }

    private fun build(city: City, method: PrayerMethod, offsets: Map<Prayer, Int>): TodayUiState {
        val now = Instant.now()
        val zone = ZoneId.of(city.zoneId)
        val today = now.atZone(zone).toLocalDate()
        val schedule = PrayerEngine.schedule(
            today, city.latitude, city.longitude, zone, method, offsets,
        )
        val next = schedule.nextAfter(now)
        val tomorrow = PrayerEngine.schedule(
            today.plusDays(1), city.latitude, city.longitude, zone, method, offsets,
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
            exactAlarmsAvailable = exactAlarmsAvailable,
        )
    }
}
