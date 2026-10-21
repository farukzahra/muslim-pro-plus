package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import java.io.File

/**
 * Audio baixado em `filesDir/audio/{sura}/` (ou numa raiz qualquer, para os
 * testes JVM). Um arquivo por verso, nome igual ao do CDN (001001.mp3).
 *
 * A ordem LRU usa o `lastModified` do arquivo: tocar um verso marca o arquivo
 * como o mais recente; [evict] apaga os mais antigos quando o total passa do
 * limite.
 */
class QuranAudioStore(
    private val root: File,
    private val source: QuranAudioSource = EveryAyahSource,
    private val limitBytes: Long = AudioCache.LIMIT_BYTES,
) {
    constructor(context: Context) : this(File(context.filesDir, "audio"))

    fun surahDir(surah: Int): File = File(root, source.pad(surah))

    fun fileFor(surah: Int, ayah: Int): File = File(surahDir(surah), source.fileNameFor(surah, ayah))

    /** Arquivo temporario do download: vira o definitivo so no fim. */
    fun partFor(surah: Int, ayah: Int): File =
        File(surahDir(surah), source.fileNameFor(surah, ayah) + ".part")

    /** Caminho pronto para tocar, ou `null` se o verso nao foi baixado. */
    fun localAudio(surah: Int, ayah: Int): File? =
        fileFor(surah, ayah).takeIf { it.isFile && it.length() > 0 }

    fun isSurahSaved(surah: Int, verseCount: Int): Boolean =
        verseCount > 0 && (1..verseCount).all { localAudio(surah, it) != null }

    fun touch(surah: Int, ayah: Int) {
        localAudio(surah, ayah)?.setLastModified(System.currentTimeMillis())
    }

    fun prepareDir(surah: Int) {
        root.mkdirs()
        surahDir(surah).mkdirs()
    }

    /**
     * Aplica a politica LRU. [keepSurah] nunca e removida: a sura em uso nao
     * pode sumir no meio da reproducao. Devolve os caminhos apagados.
     */
    fun evict(keepSurah: Int? = null): List<String> {
        val kept = keepSurah?.let { surahDir(it).absolutePath }
        val candidates = root.walkTopDown()
            .filter { it.isFile && it.extension == "mp3" && it.parentFile?.absolutePath != kept }
            .toList()
        val doomed = AudioCache.planEviction(AudioCache.snapshot(candidates), limitBytes)
        doomed.forEach { path ->
            val file = File(path)
            file.delete()
            // Remove a pasta da sura quando ela ficou vazia.
            file.parentFile?.takeIf { it.listFiles().isNullOrEmpty() }?.delete()
        }
        return doomed
    }

    fun totalBytes(): Long = AudioCache.snapshot(mp3Files()).sumOf { it.bytes }

    private fun mp3Files(): List<File> =
        root.walkTopDown().filter { it.isFile && it.extension == "mp3" }.toList()
}
