package com.farukzahra.muslimplus.quran

import com.farukzahra.muslimplus.ServiceLocator
import java.time.LocalDate

/**
 * Verso do dia: lista curada embarcada no codigo, girada pela data.
 *
 * A lista e' curta de proposito e so com versos de contexto seguro
 * (misericordia, paciencia, gratidao, oracao). A rotacao e' deterministica:
 * `toEpochDay() % size`, entao o mesmo dia sempre mostra o mesmo verso e dias
 * consecutivos avancam um indice (com wrap-around).
 */
object DailyVerse {
    data class Key(val surah: Int, val ayah: Int)

    /**
     * 63 pares (sura, ayah) verificados por `DailyVerseTest`. Todos os textos
     * têm no máximo 120 caracteres árabes: verso longo estourava o card e
     * escondia a tradução.
     */
    val keys: List<Key> = listOf(
        Key(1, 1), Key(1, 2), Key(1, 3), Key(1, 4), Key(1, 5), Key(1, 6), Key(1, 7),
        Key(2, 3), Key(2, 45), Key(2, 152), Key(2, 153),
        Key(3, 8), Key(3, 139),
        Key(7, 199), Key(8, 61), Key(9, 51), Key(10, 62),
        Key(13, 24), Key(13, 28), Key(14, 7), Key(14, 40), Key(15, 49),
        Key(16, 18), Key(16, 128), Key(17, 37), Key(17, 80),
        Key(20, 25), Key(21, 83), Key(21, 107),
        Key(23, 1), Key(23, 97), Key(23, 118), Key(28, 24), Key(29, 69), Key(30, 17),
        Key(33, 3), Key(33, 41), Key(33, 70), Key(36, 58),
        Key(55, 60), Key(67, 2), Key(71, 10), Key(73, 8),
        Key(87, 14), Key(87, 15), Key(90, 17), Key(92, 5), Key(93, 5),
        Key(94, 5), Key(94, 6),
        Key(103, 1), Key(103, 2), Key(103, 3),
        Key(108, 1), Key(108, 2), Key(108, 3),
        Key(110, 1), Key(110, 2), Key(110, 3),
        Key(112, 1), Key(112, 2), Key(112, 3), Key(112, 4),
    )

    /** Indice deterministico no ciclo de [keys], inclusive para datas antes de 1970. */
    fun indexFor(date: LocalDate): Int =
        Math.floorMod(date.toEpochDay(), keys.size.toLong()).toInt()

    fun keyFor(date: LocalDate): Key = keys[indexFor(date)]

    /** Indice absoluto, com wrap-around, usado pelo "outro verso". */
    fun keyAt(index: Int): Key = keys[Math.floorMod(index, keys.size)]

    /**
     * Verso do dia resolvido no asset. Texto vazio apenas se o asset estiver
     * corrompido (o gate e' `DailyVerseTest`); a UI esconde o card nesse caso.
     */
    fun forDate(date: LocalDate): Verse = forIndex(indexFor(date))

    /** Verso do indice absoluto; o botao de refresh soma um ao indice do dia. */
    fun forIndex(index: Int): Verse {
        val key = keyAt(index)
        return ServiceLocator.quran.verse(key.surah, key.ayah)
            ?: Verse(surah = key.surah, ayah = key.ayah, text = "")
    }
}
