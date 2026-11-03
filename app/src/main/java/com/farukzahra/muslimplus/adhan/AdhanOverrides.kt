package com.farukzahra.muslimplus.adhan

import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.time.DayOfWeek

/**
 * Exceções por oração sobre a configuração global do chamado. Quatro listas
 * independentes guardadas como string no DataStore:
 *
 * - `FAJR=madinah-nafman;ISHA=makkah-mulla` (som)
 * - `FAJR=15;ISHA=0` (lembrete; 0 = desligado só nesta oração)
 * - `FAJR;MAGHRIB` (alarme completo)
 * - `FAJR=1,2,3,4,5;ISHA=5` (dias da semana; ausente = todos)
 *
 * `SUNRISE` e qualquer chave desconhecida são descartadas. Voz de Fajr em
 * oração comum (e o contrário) também cai fora, para o tathwib nunca tocar
 * fora de hora.
 */
object AdhanOverrides {
    val allDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()

    fun decodeSounds(raw: String?): Map<Prayer, AdhanSound> =
        pairs(raw).mapNotNull { (key, value) ->
            val prayer = prayerOrNull(key) ?: return@mapNotNull null
            val sound = AdhanSound.findFor(prayer, value) ?: return@mapNotNull null
            prayer to sound
        }.toMap()

    fun encodeSounds(overrides: Map<Prayer, AdhanSound>): String =
        prayers().mapNotNull { prayer ->
            overrides[prayer]?.let { "$prayer=${it.id}" }
        }.joinToString(";")

    fun decodeReminders(raw: String?): Map<Prayer, Int> =
        pairs(raw).mapNotNull { (key, value) ->
            val prayer = prayerOrNull(key) ?: return@mapNotNull null
            val minutes = value.toIntOrNull() ?: return@mapNotNull null
            if (minutes !in PrayerReminder.options) return@mapNotNull null
            prayer to minutes
        }.toMap()

    fun encodeReminders(overrides: Map<Prayer, Int>): String =
        prayers().mapNotNull { prayer ->
            overrides[prayer]?.let { "$prayer=$it" }
        }.joinToString(";")

    fun decodeAlarm(raw: String?): Set<Prayer> =
        raw.orEmpty()
            .split(';')
            .mapNotNull { prayerOrNull(it.trim()) }
            .toSet()

    fun encodeAlarm(enabled: Set<Prayer>): String =
        prayers().filter { it in enabled }.joinToString(";") { it.name }

    fun decodeRepeats(raw: String?): Map<Prayer, Set<DayOfWeek>> =
        pairs(raw).mapNotNull { (key, value) ->
            val prayer = prayerOrNull(key) ?: return@mapNotNull null
            val days = value.split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..7 }
                .map { DayOfWeek.of(it) }
                .toSet()
            prayer to (days.ifEmpty { allDays })
        }.toMap()

    /** Só grava quem difere de "todos os dias", para a preferência ficar curta. */
    fun encodeRepeats(overrides: Map<Prayer, Set<DayOfWeek>>): String =
        prayers().mapNotNull { prayer ->
            val days = overrides[prayer]?.takeIf { it.isNotEmpty() && it != allDays } ?: return@mapNotNull null
            val encoded = days.sortedBy { it.value }.joinToString(",") { it.value.toString() }
            "$prayer=$encoded"
        }.joinToString(";")

    private fun prayers(): List<Prayer> = Prayer.entries.filter { it != Prayer.SUNRISE }

    private fun prayerOrNull(name: String): Prayer? =
        prayers().firstOrNull { it.name == name }

    private fun pairs(raw: String?): List<Pair<String, String>> =
        raw.orEmpty().split(';').mapNotNull { item ->
            val at = item.indexOf('=')
            if (at <= 0) return@mapNotNull null
            item.substring(0, at).trim() to item.substring(at + 1).trim()
        }
}
