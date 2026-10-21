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

    /** 100 pares (sura, ayah) verificados por `DailyVerseTest`. */
    val keys: List<Key> = listOf(
        Key(1, 5), Key(2, 3), Key(2, 45), Key(2, 152), Key(2, 153),
        Key(2, 177), Key(2, 186), Key(2, 216), Key(2, 261), Key(2, 286),
        Key(3, 8), Key(3, 31), Key(3, 134), Key(3, 139), Key(3, 159),
        Key(3, 173), Key(4, 58), Key(4, 135), Key(5, 8), Key(5, 35),
        Key(6, 17), Key(7, 31), Key(7, 56), Key(7, 96), Key(7, 199),
        Key(8, 46), Key(8, 61), Key(9, 40), Key(9, 51), Key(9, 105),
        Key(10, 57), Key(10, 62), Key(11, 88), Key(11, 114), Key(12, 87),
        Key(13, 11), Key(13, 24), Key(13, 28), Key(14, 7), Key(14, 34),
        Key(14, 40), Key(15, 49), Key(16, 18), Key(16, 90), Key(16, 97),
        Key(16, 128), Key(17, 23), Key(17, 37), Key(17, 70), Key(17, 80),
        Key(18, 10), Key(20, 25), Key(20, 114), Key(21, 83), Key(21, 84),
        Key(21, 87), Key(21, 107), Key(22, 78), Key(23, 1), Key(23, 97),
        Key(23, 118), Key(24, 22), Key(24, 35), Key(25, 63), Key(25, 70),
        Key(25, 74), Key(28, 24), Key(29, 45), Key(29, 69), Key(30, 17),
        Key(30, 21), Key(31, 14), Key(31, 17), Key(33, 3), Key(33, 21),
        Key(33, 41), Key(33, 70), Key(34, 39), Key(36, 58), Key(39, 9),
        Key(39, 10), Key(39, 53), Key(40, 60), Key(41, 30), Key(49, 13),
        Key(55, 60), Key(64, 11), Key(65, 3), Key(67, 2), Key(71, 10),
        Key(73, 8), Key(87, 14), Key(87, 15), Key(90, 17), Key(92, 5),
        Key(93, 5), Key(94, 5), Key(103, 3), Key(112, 1), Key(112, 2),
    )

    /** Indice deterministico no ciclo de [keys], inclusive para datas antes de 1970. */
    fun indexFor(date: LocalDate): Int =
        Math.floorMod(date.toEpochDay(), keys.size.toLong()).toInt()

    fun keyFor(date: LocalDate): Key = keys[indexFor(date)]

    /**
     * Verso do dia resolvido no asset. Texto vazio apenas se o asset estiver
     * corrompido (o gate e' `DailyVerseTest`); a UI esconde o card nesse caso.
     */
    fun forDate(date: LocalDate): Verse {
        val key = keyFor(date)
        return ServiceLocator.quran.verse(key.surah, key.ayah)
            ?: Verse(surah = key.surah, ayah = key.ayah, text = "")
    }
}
