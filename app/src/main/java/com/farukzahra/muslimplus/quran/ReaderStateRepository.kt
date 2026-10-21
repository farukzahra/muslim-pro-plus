package com.farukzahra.muslimplus.quran

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readerStateStore by preferencesDataStore(name = "reader_state")

/**
 * Ultima leitura e marcadores do Alcorao em DataStore (arquivo proprio, o
 * "settings" continua so com preferencias). Room entra numa task posterior do
 * plano; aqui basta um par chave/valor, sem migration.
 *
 * Toda escrita passa por [VerseRef], entao valores fora de 1..114 / 1..286
 * sao ignorados em vez de gravados.
 */
class ReaderStateRepository(context: Context) {
    private val store = context.applicationContext.readerStateStore

    private val lastReadKey = stringPreferencesKey("last_read")
    private val bookmarksKey = stringSetPreferencesKey("bookmarks")

    val lastRead: Flow<VerseRef?> =
        store.data.map { preferences -> VerseRef.parse(preferences[lastReadKey]) }

    val bookmarks: Flow<Set<VerseRef>> =
        store.data.map { preferences -> VerseRef.decodeAll(preferences[bookmarksKey]) }

    suspend fun saveLastRead(surah: Int, ayah: Int) {
        val ref = VerseRef.of(surah, ayah) ?: return
        store.edit { preferences -> preferences[lastReadKey] = ref.toKey() }
    }

    /** Leitura + escrita no mesmo `edit`: toques seguidos nao perdem marcador. */
    suspend fun toggleBookmark(surah: Int, ayah: Int) {
        val ref = VerseRef.of(surah, ayah) ?: return
        store.edit { preferences ->
            val current = VerseRef.decodeAll(preferences[bookmarksKey]).toMutableSet()
            if (!current.add(ref)) current.remove(ref)
            preferences[bookmarksKey] = VerseRef.encodeAll(current)
        }
    }
}
