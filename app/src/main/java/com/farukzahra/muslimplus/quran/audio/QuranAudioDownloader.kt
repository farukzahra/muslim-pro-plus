package com.farukzahra.muslimplus.quran.audio

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CancellationException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Uma requisicao HTTP, isolada para os testes trocarem por fake sem rede. */
fun interface AudioFetcher {
    /** Baixa [url] para [destination]; lanca [IOException] em qualquer falha. */
    fun fetch(url: String, destination: File)
}

class HttpAudioFetcher : AudioFetcher {
    override fun fetch(url: String, destination: File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.instanceFollowRedirects = true
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code em $url")
            connection.inputStream.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 20_000
    }
}

data class AudioDownloadResult(
    val downloaded: Int,
    val failed: Int,
) {
    val isComplete: Boolean get() = failed == 0
}

/**
 * Baixa todos os versos de uma sura para o cache local. Versos ja presentes sao
 * pulados (permite retomar um download interrompido) e o arquivo definitivo so
 * aparece depois que o `.part` foi escrito inteiro.
 */
class QuranAudioDownloader(
    private val store: QuranAudioStore,
    private val source: QuranAudioSource = EveryAyahSource,
    private val fetcher: AudioFetcher = HttpAudioFetcher(),
) {
    suspend fun download(
        surah: Int,
        verseCount: Int,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): AudioDownloadResult = withContext(Dispatchers.IO) {
        var downloaded = 0
        var failed = 0
        var processed = 0

        for (ayah in 1..verseCount) {
            coroutineContext.ensureActive()
            if (store.localAudio(surah, ayah) != null) {
                downloaded++
                processed++
                onProgress(processed, verseCount)
                continue
            }

            val target = store.fileFor(surah, ayah)
            val part = store.partFor(surah, ayah)
            try {
                store.prepareDir(surah)
                fetcher.fetch(source.urlFor(surah, ayah), part)
                coroutineContext.ensureActive()
                if (part.length() <= 0) throw IOException("audio vazio: ${part.name}")
                if (!part.renameTo(target)) {
                    part.copyTo(target, overwrite = true)
                    part.delete()
                }
                store.touch(surah, ayah)
                downloaded++
            } catch (cancelled: CancellationException) {
                part.delete()
                throw cancelled
            } catch (error: Exception) {
                part.delete()
                failed++
            }
            processed++
            onProgress(processed, verseCount)
        }

        // Mantem o limite de 300 MB; a sura recem-baixada fica protegida.
        store.evict(keepSurah = surah)
        AudioDownloadResult(downloaded = downloaded, failed = failed)
    }
}
