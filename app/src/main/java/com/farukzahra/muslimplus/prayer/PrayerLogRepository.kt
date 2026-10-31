package com.farukzahra.muslimplus.prayer

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prayerLogStore by preferencesDataStore(name = "prayer_log")

/**
 * Rastreador de orações em DataStore. Só grava as cinco orações obrigatórias,
 * por data local da cidade escolhida, e poda os dias fora da retenção.
 *
 * Todas as escritas fazem leitura + alteração dentro do mesmo `edit`, então
 * toques seguidos no check não perdem marcação.
 */
class PrayerLogRepository(context: Context) {
    private val store = context.applicationContext.prayerLogStore
    private val daysKey = stringPreferencesKey("days")

    val days: Flow<Map<LocalDate, Set<Prayer>>> =
        store.data.map { preferences -> PrayerLog.decodeDays(preferences[daysKey]) }

    suspend fun toggle(date: LocalDate, prayer: Prayer) {
        if (prayer !in PrayerLog.trackable) return
        update(date) { current ->
            if (prayer in current) current - prayer else current + prayer
        }
    }

    suspend fun setAll(date: LocalDate, done: Boolean) {
        update(date) { if (done) PrayerLog.trackable.toSet() else emptySet() }
    }

    private suspend fun update(
        date: LocalDate,
        transform: (Set<Prayer>) -> Set<Prayer>,
    ) {
        store.edit { preferences ->
            val all = PrayerLog.decodeDays(preferences[daysKey]).toMutableMap()
            val next = PrayerLog.clean(transform(PrayerLog.clean(all[date].orEmpty())))
            if (next.isEmpty()) all.remove(date) else all[date] = next
            // A poda usa a data marcada, não o fuso do sistema: o dia do
            // rastreador é o da cidade escolhida.
            preferences[daysKey] = PrayerLog.encodeDays(PrayerLog.prune(all, date))
        }
    }
}
