package com.farukzahra.muslimplus.quran.audio

import java.io.File

/** Arquivo de audio ja no disco, com o carimbo usado como recencia (LRU). */
data class CachedAudio(
    val id: String,
    val bytes: Long,
    val lastUsedMs: Long,
)

/**
 * Politica de cache de audio, sem Android e sem disco: recebe o tamanho dos
 * arquivos e devolve quais precisam sair. Assim a regra LRU pode ser testada
 * em JVM, sem rede e sem emulador.
 */
object AudioCache {
    /** Limite total do audio baixado. */
    const val LIMIT_BYTES: Long = 300L * 1024 * 1024

    /**
     * Devolve os ids a apagar para o total caber em [limitBytes].
     *
     * Abaixo (ou no limite) nao remove nada. Acima, remove do mais antigo para
     * o mais novo ate o excedente zerar. [CachedAudio.id] desempata de forma
     * deterministica quando dois arquivos tem o mesmo carimbo.
     */
    fun planEviction(sizes: List<CachedAudio>, limitBytes: Long = LIMIT_BYTES): List<String> {
        val total = sizes.sumOf { it.bytes.coerceAtLeast(0) }
        if (total <= limitBytes) return emptyList()

        var excess = total - limitBytes
        val evicted = mutableListOf<String>()
        for (entry in sizes.sortedWith(compareBy<CachedAudio> { it.lastUsedMs }.thenBy { it.id })) {
            if (excess <= 0) break
            evicted += entry.id
            excess -= entry.bytes.coerceAtLeast(0)
        }
        return evicted
    }

    /** Ids de [files] no formato aceito por [planEviction]. */
    fun snapshot(files: List<File>): List<CachedAudio> =
        files.map { CachedAudio(it.absolutePath, it.length(), it.lastModified()) }
}
