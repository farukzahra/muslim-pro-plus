package com.farukzahra.muslimplus.settings

/**
 * Offset em dias aplicado a data hijri exibida na aba Hoje.
 *
 * A contagem pode variar por pais (observacao da lua), entao o usuario escolhe
 * -1, 0 ou +1. Qualquer outro valor salvo ou lido vira [DEFAULT], para um
 * DataStore antigo/corrompido nunca quebrar a tela.
 */
object HijriOffset {
    const val DEFAULT = 0

    fun isValid(days: Int): Boolean = days in -1..1

    fun sanitize(days: Int): Int = if (isValid(days)) days else DEFAULT
}
