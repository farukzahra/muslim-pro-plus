package com.farukzahra.muslimplus.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
    private val cities: CityCatalog = CityCatalog.fromAsset(context),
) {
    private val cityKey = stringPreferencesKey("city_id")
    private val methodKey = stringPreferencesKey("prayer_method")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val quranModesKey = stringSetPreferencesKey("quran_modes")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val prayerOffsetsKey = stringPreferencesKey("prayer_offsets")
    private val hijriOffsetKey = intPreferencesKey("hijri_offset")
    private val onboardingDoneKey = booleanPreferencesKey(Onboarding.DONE_KEY)

    /**
     * Cidade escolhida ou, sem escolha (primeira execucao), a maior cidade do
     * fuso do aparelho. Id salvo que o catalogo nao conhece (formato antigo)
     * tambem cai no padrao do fuso. Sem cidade no fuso, a primeira do catalogo.
     */
    val cityId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[cityKey]?.let(cities::byIdOrNull)?.id ?: defaultCityId()
    }
    val methodName: Flow<String> = context.dataStore.data.map { it[methodKey] ?: "MWL" }
    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[notificationsKey] ?: true }
    val quranModes: Flow<Set<String>> =
        context.dataStore.data.map { it[quranModesKey] ?: setOf("ar") }
    val themeMode: Flow<String> =
        context.dataStore.data.map { it[themeKey] ?: "dark" }
    val prayerOffsets: Flow<Map<Prayer, Int>> =
        context.dataStore.data.map { preferences ->
            PrayerOffsets.decode(preferences[prayerOffsetsKey])
        }
    val hijriOffset: Flow<Int> =
        context.dataStore.data.map { preferences ->
            HijriOffset.sanitize(preferences[hijriOffsetKey] ?: HijriOffset.DEFAULT)
        }

    /** `null` = primeira execucao (onboarding ainda nao foi concluido ou pulado). */
    val onboardingDone: Flow<Boolean?> =
        context.dataStore.data.map { it[onboardingDoneKey] }

    suspend fun setCity(id: String) {
        context.dataStore.edit { it[cityKey] = id }
    }

    suspend fun setMethod(name: String) {
        context.dataStore.edit { it[methodKey] = name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[notificationsKey] = enabled }
    }

    suspend fun setQuranModes(modes: Set<String>) {
        context.dataStore.edit { it[quranModesKey] = modes }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[themeKey] = mode }
    }

    /** Aceita apenas -1, 0 e +1; valores fora da faixa viram o padrao. */
    suspend fun setHijriOffset(days: Int) {
        context.dataStore.edit { it[hijriOffsetKey] = HijriOffset.sanitize(days) }
    }

    /** Grava a flag tanto no "Comecar" quanto no "Pular". */
    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[onboardingDoneKey] = true }
    }

    suspend fun setPrayerOffset(prayer: Prayer, minutes: Int) {
        updateOffset(prayer) { PrayerOffsets.clamp(minutes) }
    }

    /**
     * Soma [delta] ao valor salvo dentro do mesmo `edit`, para que toques
     * seguidos no dialogo nao percam incremento por estado atrasado da UI.
     */
    suspend fun nudgePrayerOffset(prayer: Prayer, delta: Int) {
        updateOffset(prayer) { previous -> PrayerOffsets.clamp(previous + delta) }
    }

    private suspend fun updateOffset(prayer: Prayer, value: (Int) -> Int) {
        context.dataStore.edit { preferences ->
            val current = PrayerOffsets.decode(preferences[prayerOffsetsKey])
            val updated = current.toMutableMap()
            updated[prayer] = value(current[prayer] ?: 0)
            preferences[prayerOffsetsKey] = PrayerOffsets.encode(updated)
        }
    }

    private fun defaultCityId(): String =
        cities.defaultForZone(ZoneId.systemDefault().id)?.id ?: cities.first().id
}
