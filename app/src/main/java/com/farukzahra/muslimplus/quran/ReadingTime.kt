package com.farukzahra.muslimplus.quran

import java.time.LocalDate

/**
 * Tempo de leitura por dia, medido enquanto o leitor está em primeiro plano.
 * Fica no mesmo DataStore do leitor, sem conta e sem serviço de fundo.
 */
object ReadingTime {
    const val RETENTION_DAYS = 60

    /** Nada além de 12 h por dia: protege contra relógio errado ou tela presa. */
    const val MAX_SECONDS_PER_DAY = 12 * 60 * 60

    fun prune(
        days: Map<LocalDate, Int>,
        today: LocalDate,
        retention: Int = RETENTION_DAYS,
    ): Map<LocalDate, Int> {
        val oldest = today.minusDays((retention - 1).coerceAtLeast(0).toLong())
        return days.filterKeys { !it.isBefore(oldest) && !it.isAfter(today) }
    }

    fun add(
        days: Map<LocalDate, Int>,
        date: LocalDate,
        seconds: Int,
        today: LocalDate = date,
    ): Map<LocalDate, Int> {
        if (seconds <= 0) return days
        val updated = days.toMutableMap()
        updated[date] = ((days[date] ?: 0) + seconds).coerceIn(0, MAX_SECONDS_PER_DAY)
        return prune(updated, today)
    }

    fun minutes(days: Map<LocalDate, Int>, date: LocalDate): Int =
        (days[date] ?: 0) / 60

    /**
     * Dias seguidos com pelo menos [minSeconds] de leitura. Mesma regra do
     * rastreador de orações: hoje em andamento não apaga a sequência de ontem.
     */
    fun streak(
        days: Map<LocalDate, Int>,
        today: LocalDate,
        minSeconds: Int = 60,
    ): Int {
        var cursor = today
        if ((days[cursor] ?: 0) < minSeconds) cursor = cursor.minusDays(1)
        var count = 0
        while ((days[cursor] ?: 0) >= minSeconds) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    /** Formato `2026-10-31=125;2026-11-01=60`. */
    fun encode(days: Map<LocalDate, Int>): String =
        days.entries
            .filter { (_, seconds) -> seconds > 0 }
            .sortedBy { (date, _) -> date }
            .joinToString(";") { (date, seconds) -> "$date=$seconds" }

    fun decode(raw: String?): Map<LocalDate, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(';')
            .mapNotNull { entry ->
                val date = runCatching { LocalDate.parse(entry.substringBefore('=', "")) }
                    .getOrNull() ?: return@mapNotNull null
                val seconds = entry.substringAfter('=', "").toIntOrNull() ?: return@mapNotNull null
                if (seconds <= 0) null else date to seconds.coerceAtMost(MAX_SECONDS_PER_DAY)
            }
            .toMap()
    }
}
