package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.farukzahra.muslimplus.quran.VerseRef
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Cache de reprodução descartável. Downloads explícitos ficam em filesDir/audio. */
@androidx.annotation.OptIn(UnstableApi::class)
object QuranStreamingCache {
    @Volatile private var instance: SimpleCache? = null

    /** Fila guarda a URL remota; a cada abertura o arquivo baixado tem prioridade. */
    private val everyAyahUrl =
        Regex("""^https://everyayah\.com/data/([A-Za-z0-9_-]+)/(\d{3})(\d{3})\.mp3$""")

    @Synchronized
    private fun cache(context: Context): SimpleCache = instance ?: SimpleCache(
        File(context.cacheDir, "quran-streaming"),
        LeastRecentlyUsedCacheEvictor(AudioCache.LIMIT_BYTES),
        StandaloneDatabaseProvider(context.applicationContext),
    ).also { instance = it }

    private fun localFile(context: Context, spec: DataSpec): File? {
        if (spec.uri.scheme != "https") return null
        val groups = everyAyahUrl.find(spec.uri.toString())?.groupValues ?: return null
        val voice = QuranReciters.all.find { it.folder == groups[1] } ?: return null
        val surah = groups[2].toInt()
        val ayah = groups[3].toInt()
        if (VerseRef.of(surah, ayah) == null) return null
        if (ayah > QuranReciters.verseCounts[surah - 1]) return null
        return QuranAudioStore(context, voice).localAudio(surah, ayah)
    }

    /**
     * Arquivo baixado abre direto, sem entrar no cache; o resto passa pelo cache
     * LRU. A escolha acontece em cada open, então baixar ou apagar depois de
     * enfileirar continua funcionando.
     */
    fun factory(
        context: Context,
        upstream: DataSource.Factory = DefaultDataSource.Factory(context.applicationContext),
    ): DataSource.Factory = DataSource.Factory {
        LocalFirstDataSource(
            cached = {
                CacheDataSource.Factory()
                    .setCache(cache(context))
                    .setUpstreamDataSourceFactory(upstream)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    .createDataSource()
            },
            localFile = { spec -> localFile(context, spec) },
        )
    }

    fun bytes(context: Context): Long = cache(context).cacheSpace

    suspend fun clear(context: Context) {
        // Libera o escritor antes de remover spans; a próxima reprodução prepara a fila novamente.
        withContext(Dispatchers.Main) { QuranPlaybackService.pauseForMaintenance() }
        withContext(Dispatchers.IO) {
            val cache = cache(context)
            cache.keys.toList().forEach(cache::removeResource)
        }
    }

    /** Download explícito tem prioridade e nunca é gravado de novo no cache. */
    private class LocalFirstDataSource(
        private val cached: () -> DataSource,
        private val localFile: (DataSpec) -> File?,
    ) : DataSource {
        private var delegate: DataSource? = null
        private var cacheSource: DataSource? = null
        private var cacheHasListeners = false
        private val listeners = mutableListOf<TransferListener>()

        override fun addTransferListener(transferListener: TransferListener) {
            listeners += transferListener
            delegate?.addTransferListener(transferListener)
        }

        override fun open(dataSpec: DataSpec): Long {
            val local = localFile(dataSpec)
            val target = if (local != null) {
                FileDataSource()
            } else {
                cacheSource ?: cached().also { cacheSource = it }
            }
            delegate = target
            when {
                local != null -> listeners.forEach(target::addTransferListener)
                !cacheHasListeners -> {
                    listeners.forEach(target::addTransferListener)
                    cacheHasListeners = true
                }
            }
            return target.open(if (local != null) dataSpec.withUri(local.toUri()) else dataSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            checkNotNull(delegate).read(buffer, offset, length)

        override fun getUri(): android.net.Uri = checkNotNull(checkNotNull(delegate).uri)

        override fun getResponseHeaders(): Map<String, List<String>> =
            checkNotNull(delegate).responseHeaders

        override fun close() {
            delegate?.close()
            delegate = null
        }
    }
}
