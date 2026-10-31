package com.farukzahra.muslimplus.prayer

import com.farukzahra.muslimplus.location.City
import java.time.Instant
import java.time.ZoneId

/**
 * Lembrete antes do adhan. É um alarme separado do chamado: o aviso chega
 * antes, sem áudio, e o adhan continua tocando no horário exato.
 */
object PrayerReminder {
    /** 0 = desligado. */
    val options: List<Int> = listOf(0, 5, 10, 15, 30)

    const val OFF = 0

    fun sanitize(minutes: Int?): Int = minutes?.takeIf { it in options } ?: OFF

    fun isOff(minutes: Int?): Boolean = sanitize(minutes) == OFF

    /**
     * Próximo aviso a partir de [now]: a oração mais próxima cujo
     * `horário - minutos` ainda está no futuro. O `at` devolvido é o do
     * **aviso**, não o da oração; quem precisa do horário da oração soma os
     * minutos de volta.
     */
    fun next(
        now: Instant,
        city: City,
        method: PrayerMethod,
        offsets: Map<Prayer, Int>,
        minutes: Int,
    ): NextPrayer? {
        val gap = sanitize(minutes)
        if (gap == OFF) return null
        val zone = ZoneId.of(city.zoneId)
        val today = now.atZone(zone).toLocalDate()
        val shift = gap.toLong() * 60L
        return sequenceOf(today, today.plusDays(1))
            .flatMap { date ->
                val schedule = PrayerEngine.schedule(
                    date, city.latitude, city.longitude, zone, method, offsets,
                )
                Prayer.entries
                    .asSequence()
                    .filter { it != Prayer.SUNRISE }
                    .mapNotNull { prayer ->
                        schedule.timeOf(prayer)?.minusSeconds(shift)?.let { at -> prayer to at }
                    }
            }
            .filter { (_, at) -> at.isAfter(now) }
            .minByOrNull { (_, at) -> at }
            ?.let { (prayer, at) -> NextPrayer(prayer, at) }
    }
}
