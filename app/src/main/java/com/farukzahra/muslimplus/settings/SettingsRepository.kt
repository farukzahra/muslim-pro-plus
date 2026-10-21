package com.farukzahra.muslimplus.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val cityKey = stringPreferencesKey("city_id")
    private val methodKey = stringPreferencesKey("prayer_method")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val quranModesKey = stringSetPreferencesKey("quran_modes")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val prayerOffsetsKey = stringPreferencesKey("prayer_offsets")

    val cityId: Flow<String> = context.dataStore.data.map { it[cityKey] ?: "sao-paulo" }
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
}
