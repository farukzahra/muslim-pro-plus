package com.farukzahra.muslimplus.quran.audio

/**
 * Origem do audio de cada verso.
 *
 * O player so conhece esta interface: trocar de CDN (ou de recitador) e mudar
 * a implementacao, sem tocar no ExoPlayer, no download nem na tela.
 */
interface QuranAudioSource {
    /** Nome exibido na notificacao. E uma constante de codigo, nao string de UI. */
    val reciter: String

    /** URL remota do verso. Funcao pura: mesmos argumentos, mesma string. */
    fun urlFor(surah: Int, ayah: Int): String

    /** Nome do arquivo local, derivado da mesma numeracao da URL. */
    fun fileNameFor(surah: Int, ayah: Int): String = "${pad(surah)}${pad(ayah)}.mp3"

    /** Zero-padding de 3 digitos usado por todas as fontes conhecidas. */
    fun pad(value: Int): String = value.toString().padStart(3, '0')
}

/**
 * everyayah.com, recitador Mishary Alafasy (128 kbps).
 *
 * Formato: `{sura3}{ayah3}.mp3`, com sura e ayah em 3 digitos.
 * Ex.: 1:1 -> 001001.mp3, 2:255 -> 002255.mp3, 114:6 -> 114006.mp3.
 */
object EveryAyahSource : QuranAudioSource {
    const val BASE_URL = "https://everyayah.com/data/Alafasy_128kbps/"

    override val reciter: String = "Mishary Alafasy"

    override fun urlFor(surah: Int, ayah: Int): String = BASE_URL + fileNameFor(surah, ayah)
}
