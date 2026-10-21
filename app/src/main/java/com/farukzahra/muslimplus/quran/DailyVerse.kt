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

    /** 30 pares (sura, ayah) verificados por `DailyVerseTest`. */
    val keys: List<Key> = listOf(
        Key(1, 5), // So a Ti adoramos e a Ti pedimos ajuda
        Key(2, 45), // Buscai ajuda na paciencia e na oracao
        Key(2, 152), // Lembrai-vos de Mim, que Eu Me lembrarei de vos
        Key(2, 153), // Deus esta com os pacientes
        Key(3, 31), // Se amais a Deus, segui-me
        Key(3, 139), // Nao desanimeis nem vos entristecais
        Key(5, 35), // Temei a Deus e buscai o meio de aproximar-vos
        Key(7, 56), // Invocai-O com temor e esperanca
        Key(8, 46), // Sede pacientes; Deus esta com os pacientes
        Key(10, 57), // Chegou-vos uma exortacao do vosso Senhor
        Key(11, 114), // As boas obras apagam as mas
        Key(13, 28), // Com a lembranca de Deus os coracoes se tranquilizam
        Key(14, 40), // Senhor, faze-me cumprir a oracao
        Key(16, 128), // Deus esta com os que temem e fazem o bem
        Key(17, 80), // Senhor, faze-me entrar com verdade
        Key(21, 107), // Nao te enviamos senao como misericordia para os mundos
        Key(23, 1), // Bem-aventurados os fieis
        Key(23, 118), // Senhor, perdoa e tem misericordia
        Key(25, 74), // Senhor, concede-nos consolo nos nossos conjuges e filhos
        Key(29, 69), // Deus esta com os benfeitores
        Key(31, 17), // Cumpre a oracao e ordena o bem
        Key(33, 3), // Confia em Deus; Deus basta como protector
        Key(33, 41), // Lembrai-vos de Deus com frequencia
        Key(87, 14), // Bem-aventurado quem se purifica
        Key(87, 15), // E lembra o nome do seu Senhor e ora
        Key(93, 5), // O teu Senhor te dara e ficaras satisfeito
        Key(94, 5), // Com a dificuldade vem a facilidade
        Key(103, 3), // Os que creem, praticam o bem e recomendam a verdade
        Key(112, 1), // Dize: Ele e Deus, o Unico
        Key(112, 2), // Deus, o Absoluto
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
