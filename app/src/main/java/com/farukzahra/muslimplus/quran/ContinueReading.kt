package com.farukzahra.muslimplus.quran

/**
 * Regras puras de "continuar de onde parei". Ficam fora do ViewModel para
 * serem testadas em JVM: a ultima leitura so vale se a sura existe no catalogo
 * e se o verso existe naquela sura (1..verseCount).
 */
object ContinueReading {
    /** Ultima leitura valida no catalogo, ou `null` se nao ha card a mostrar. */
    fun resolve(lastRead: VerseRef?, surahs: List<Surah>): VerseRef? {
        val ref = lastRead ?: return null
        val surah = surahs.firstOrNull { it.id == ref.surah } ?: return null
        return ref.takeIf { it.ayah in 1..surah.verseCount }
    }

    /** Verso inicial no leitor da [surah], somente quando a ultima leitura e dela. */
    fun startAyah(lastRead: VerseRef?, surah: Surah): Int? {
        val ref = lastRead ?: return null
        if (ref.surah != surah.id || ref.ayah !in 1..surah.verseCount) return null
        return ref.ayah
    }

    /** Indice na lista de versos carregada, ou `null` se o verso nao estiver nela. */
    fun verseIndex(verses: List<Verse>, ayah: Int): Int? =
        verses.indexOfFirst { it.ayah == ayah }.takeIf { it >= 0 }
}
