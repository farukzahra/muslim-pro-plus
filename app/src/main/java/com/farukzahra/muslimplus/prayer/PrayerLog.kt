package com.farukzahra.muslimplus.prayer

import java.time.LocalDate

/**
 * Um dia de rastreamento: só as cinco orações obrigatórias entram. Nascer do
 * sol não é oração e nunca aparece aqui.
 */
data class PrayerDay(
    val date: LocalDate,
    val done: Set<Prayer>,
) {
    val count: Int get() = PrayerLog.progress(done)
    val complete: Boolean get() = PrayerLog.isComplete(done)
}

/**
 * Regras puras do rastreador de orações: conjunto válido, progresso, sequência
 * de dias completos e codificação para o DataStore. Fica fora do ViewModel e do
 * Android para rodar em teste JVM.
 */
object PrayerLog {
    val trackable: List<Prayer> = listOf(
        Prayer.FAJR,
        Prayer.DHUHR,
        Prayer.ASR,
        Prayer.MAGHRIB,
        Prayer.ISHA,
    )

    /** Dias guardados; o resto é podado na próxima escrita. */
    const val RETENTION_DAYS = 60

    /** Ignora nomes desconhecidos (versões antigas, nascer do sol, lixo). */
    fun sanitize(done: Set<String>?): Set<Prayer> =
        trackable.filter { it.name in done.orEmpty() }.toSet()

    /** Só as cinco obrigatórias, sem conversão de nome. */
    fun clean(done: Set<Prayer>): Set<Prayer> =
        done.filter { it in trackable }.toSet()

    fun encode(done: Set<Prayer>): Set<String> =
        done.filter { it in trackable }.map { it.name }.toSet()

    fun progress(done: Set<Prayer>): Int = done.count { it in trackable }

    fun isComplete(done: Set<Prayer>): Boolean = progress(done) == trackable.size

    /**
     * Últimos [length] dias terminando em [end], em ordem crescente. Dias sem
     * registro aparecem com o conjunto vazio, para a faixa não abrir buracos.
     */
    fun week(
        end: LocalDate,
        days: Map<LocalDate, Set<Prayer>>,
        length: Int = 7,
    ): List<PrayerDay> {
        if (length <= 0) return emptyList()
        return (length - 1 downTo 0).map { offset ->
            val date = end.minusDays(offset.toLong())
            PrayerDay(date, clean(days[date].orEmpty()))
        }
    }

    /**
     * Dias completos consecutivos terminando hoje. Se hoje ainda está
     * incompleto, a contagem começa em ontem e a sequência de ontem não é
     * perdida por causa do dia em andamento.
     */
    fun streak(days: Map<LocalDate, Set<Prayer>>, today: LocalDate): Int {
        var cursor = today
        if (!isComplete(clean(days[cursor].orEmpty()))) {
            cursor = cursor.minusDays(1)
        }
        var count = 0
        while (isComplete(clean(days[cursor].orEmpty()))) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun prune(
        days: Map<LocalDate, Set<Prayer>>,
        today: LocalDate,
        retention: Int = RETENTION_DAYS,
    ): Map<LocalDate, Set<Prayer>> {
        val oldest = today.minusDays((retention - 1).coerceAtLeast(0).toLong())
        return days.filterKeys { !it.isBefore(oldest) && !it.isAfter(today.plusDays(1)) }
    }

    /** Formato `2026-10-31=FAJR,DHUHR;2026-11-01=...`; dias vazios não entram. */
    fun encodeDays(days: Map<LocalDate, Set<Prayer>>): String =
        days.entries
            .map { (date, done) -> date to encode(done) }
            .filter { (_, done) -> done.isNotEmpty() }
            .sortedBy { (date, _) -> date }
            .joinToString(separator = ";") { (date, done) ->
                "${date}=${done.sorted().joinToString(",")}"
            }

    fun decodeDays(raw: String?): Map<LocalDate, Set<Prayer>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(';')
            .mapNotNull { entry ->
                val date = entry.substringBefore('=', "").let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                } ?: return@mapNotNull null
                val names = entry.substringAfter('=', "").split(',').toSet()
                val done = sanitize(names)
                if (done.isEmpty()) null else date to done
            }
            .toMap()
    }
}
