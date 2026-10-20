package com.farukzahra.muslimplus.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val cityKey = stringPreferencesKey("city_id")
    private val methodKey = stringPreferencesKey("prayer_method")

    val cityId: Flow<String> = context.dataStore.data.map { it[cityKey] ?: "sao-paulo" }
    val methodName: Flow<String> = context.dataStore.data.map { it[methodKey] ?: "MWL" }

    suspend fun setCity(id: String) {
        context.dataStore.edit { it[cityKey] = id }
    }

    suspend fun setMethod(name: String) {
        context.dataStore.edit { it[methodKey] = name }
    }
}
