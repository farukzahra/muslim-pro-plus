package com.farukzahra.muslimplus.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.notifications.ExactAlarms
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerDay
import com.farukzahra.muslimplus.prayer.PrayerEngine
import com.farukzahra.muslimplus.prayer.PrayerLog
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
    val hijriOffset: Int,
    /** Orações já marcadas hoje (só as cinco obrigatórias). */
    val done: Set<Prayer> = emptySet(),
    /** Últimos sete dias, do mais antigo para hoje. */
    val week: List<PrayerDay> = emptyList(),
    val streak: Int = 0,
    /** Orações cujo adhan está ligado; o sino da linha reflete isto. */
    val adhanPrayers: Set<Prayer> = emptySet(),
) {
    val doneCount: Int get() = PrayerLog.progress(done)
    val allDone: Boolean get() = PrayerLog.isComplete(done)
}

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = ServiceLocator.settings
    private val prayerLog = ServiceLocator.prayerLog
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

    /** Marca ou desmarca a oração no dia que está na tela. */
    fun togglePrayer(prayer: Prayer) {
        val current = _state.value ?: return
        if (prayer !in PrayerLog.trackable) return
        viewModelScope.launch { prayerLog.toggle(current.date, prayer) }
    }

    fun markAllPrayers() {
        val current = _state.value ?: return
        if (current.allDone) return
        viewModelScope.launch { prayerLog.setAll(current.date, done = true) }
    }

    init {
        viewModelScope.launch {
            val configFlow = combine(
                settings.cityId,
                settings.methodName,
                settings.prayerOffsets,
                settings.hijriOffset,
            ) { cityId, methodName, offsets, hijriOffset ->
                TodayConfig(cityId, methodName, offsets, hijriOffset)
            }
            combine(configFlow, prayerLog.days, settings.adhanPrayers) { config, days, adhan ->
                TodayInputs(config, days, adhan)
            }
                .distinctUntilChanged()
                .collectLatest { inputs ->
                    val city = ServiceLocator.cities.byId(inputs.config.cityId)
                    val method = runCatching { PrayerMethod.valueOf(inputs.config.methodName) }
                        .getOrDefault(PrayerMethod.MWL)
                    // collectLatest cancela este loop quando cidade, metodo,
                    // offsets, offset hijri, marcações ou adhan mudam, para o
                    // relógio não segurar a config antiga.
                    while (true) {
                        _state.value = build(city, method, inputs)
                        delay(1_000)
                    }
                }
        }
    }

    private fun build(city: City, method: PrayerMethod, inputs: TodayInputs): TodayUiState {
        val now = Instant.now()
        val zone = ZoneId.of(city.zoneId)
        val today = now.atZone(zone).toLocalDate()
        val offsets = inputs.config.offsets
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
            hijriOffset = inputs.config.hijriOffset,
            done = PrayerLog.clean(inputs.days[today].orEmpty()),
            week = PrayerLog.week(today, inputs.days),
            streak = PrayerLog.streak(inputs.days, today),
            adhanPrayers = inputs.adhanPrayers,
        )
    }
}

/** Config lida do DataStore; agrupada para o `combine` nao virar Triple aninhada. */
private data class TodayConfig(
    val cityId: String,
    val methodName: String,
    val offsets: Map<Prayer, Int>,
    val hijriOffset: Int,
)

/** Config + o que muda a cada toque do usuário (marcações e adhan). */
private data class TodayInputs(
    val config: TodayConfig,
    val days: Map<LocalDate, Set<Prayer>>,
    val adhanPrayers: Set<Prayer>,
)
