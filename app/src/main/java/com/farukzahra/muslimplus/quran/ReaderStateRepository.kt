package com.farukzahra.muslimplus.quran

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readerStateStore by preferencesDataStore(name = "reader_state")

/**
 * Última leitura, marcadores e tempo de leitura do Alcorão em DataStore
 * (arquivo próprio, o "settings" continua só com preferências). Room entra numa
 * task posterior do plano; aqui basta um par chave/valor, sem migration.
 *
 * Toda escrita de posição passa por [VerseRef], então valores fora de 1..114 /
 * 1..286 são ignorados em vez de gravados.
 */
class ReaderStateRepository(context: Context) {
    private val store = context.applicationContext.readerStateStore

    private val lastReadKey = stringPreferencesKey("last_read")
    private val bookmarksKey = stringSetPreferencesKey("bookmarks")
    private val readingTimeKey = stringPreferencesKey("reading_time")

    val lastRead: Flow<VerseRef?> =
        store.data.map { preferences -> VerseRef.parse(preferences[lastReadKey]) }

    val bookmarks: Flow<Set<VerseRef>> =
        store.data.map { preferences -> VerseRef.decodeAll(preferences[bookmarksKey]) }

    /** Segundos de leitura por dia; só os últimos 60 dias ficam. */
    val readingSeconds: Flow<Map<LocalDate, Int>> =
        store.data.map { preferences -> ReadingTime.decode(preferences[readingTimeKey]) }

    suspend fun saveLastRead(surah: Int, ayah: Int) {
        val ref = VerseRef.of(surah, ayah) ?: return
        store.edit { preferences -> preferences[lastReadKey] = ref.toKey() }
    }

    /** Leitura + escrita no mesmo `edit`: toques seguidos não perdem marcador. */
    suspend fun toggleBookmark(surah: Int, ayah: Int) {
        val ref = VerseRef.of(surah, ayah) ?: return
        store.edit { preferences ->
            val current = VerseRef.decodeAll(preferences[bookmarksKey]).toMutableSet()
            if (!current.add(ref)) current.remove(ref)
            preferences[bookmarksKey] = VerseRef.encodeAll(current)
        }
    }

    /**
     * Soma segundos ao dia [date]. O leitor chama em passos de 15 s enquanto
     * está em primeiro plano; nada roda em segundo plano.
     *
     * A poda usa [date] como referência, não o fuso do sistema: o dia de
     * leitura é o da cidade escolhida e pode estar um dia à frente do fuso do
     * aparelho (ex.: Makkah/Shanghai com emulador em UTC).
     */
    suspend fun addReadingSeconds(date: LocalDate, seconds: Int) {
        if (seconds <= 0) return
        store.edit { preferences ->
            val current = ReadingTime.decode(preferences[readingTimeKey])
            preferences[readingTimeKey] =
                ReadingTime.encode(ReadingTime.add(current, date, seconds, date))
        }
    }
}
