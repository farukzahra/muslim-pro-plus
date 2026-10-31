package com.farukzahra.muslimplus.settings

import kotlin.math.ceil

/**
 * Meta diária de leitura do Alcorão. Só minutos: é o que o usuário consegue
 * medir sem conta e sem backend, e é o que o leitor sabe cronometrar.
 */
object QuranGoal {
    val options: List<Int> = listOf(5, 10, 15, 30)

    const val DEFAULT_MINUTES = 10

    /** Ritmo médio usado só para a estimativa de conclusão. */
    const val AYAHS_PER_MINUTE = 6

    const val TOTAL_AYAHS = 6236

    fun sanitize(minutes: Int?): Int =
        minutes?.takeIf { it in options } ?: DEFAULT_MINUTES

    /** Dias estimados para ler o Alcorão inteiro no ritmo escolhido. */
    fun khatamDays(minutes: Int): Int {
        val perDay = sanitize(minutes) * AYAHS_PER_MINUTE
        return ceil(TOTAL_AYAHS.toDouble() / perDay).toInt()
    }
}
