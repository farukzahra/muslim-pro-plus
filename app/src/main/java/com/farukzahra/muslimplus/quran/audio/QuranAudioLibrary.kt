package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import com.farukzahra.muslimplus.quran.Surah
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** One process-wide writer; cancellation retains the lock until finally completes. */
data class AudioLibraryProgress(val running: Boolean = false, val reciterId: String? = null, val surah: Int? = null, val done: Int = 0, val total: Int = 0, val surahDone: Int = 0, val failed: Boolean = false)

class QuranAudioLibrary private constructor(private val context: Context, private val fetcher: AudioFetcher = HttpAudioFetcher()) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableProgress = MutableStateFlow(AudioLibraryProgress())
    val progress: StateFlow<AudioLibraryProgress> = mutableProgress
    private val mutableRevision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = mutableRevision
    private var job: Job? = null
    private var busy = false

    @OptIn(ExperimentalCoroutinesApi::class)
    @Synchronized fun download(reciter: QuranReciter, surahs: List<Surah>) {
        if (busy || surahs.isEmpty()) return
        busy = true
        mutableProgress.value = AudioLibraryProgress(true, reciter.id, total = surahs.sumOf { it.verseCount })
        job = scope.launch(start = CoroutineStart.ATOMIC) {
            var done = 0
            var failed = false
            try {
                ensureActive()
                val store = QuranAudioStore(context, reciter)
                if (surahs.size == 114) {
                    val remaining = surahs.sumOf { it.verseCount - store.savedVerses(it.id, it.verseCount) }
                    val required = requiredBytes(reciter, remaining)
                    if (context.filesDir.usableSpace < required) throw java.io.IOException("Insufficient space")
                }
                val downloader = QuranAudioDownloader(store, reciter, fetcher)
                for (surah in surahs) {
                    ensureActive()
                    val before = done
                    mutableProgress.value = mutableProgress.value.copy(surah = surah.id, surahDone = 0)
                    val result = downloader.download(surah.id, surah.verseCount) { count, _ ->
                        mutableProgress.value = mutableProgress.value.copy(done = before + count, surahDone = count)
                    }
                    done += surah.verseCount
                    failed = failed || !result.isComplete
                    mutableRevision.value++
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed = true
            } finally {
                synchronized(this@QuranAudioLibrary) {
                    mutableProgress.value = mutableProgress.value.copy(running = false, failed = failed)
                    mutableRevision.value++
                    busy = false
                    job = null
                }
            }
        }
    }

    fun refresh() { synchronized(this) { mutableRevision.value++ } }

    @Synchronized fun cancel() {
        (fetcher as? CancellableFetcher)?.cancelActive()
        job?.cancel()
    }

    /** Deletion participates in the same exclusion as downloads. */
    suspend fun delete(reciter: QuranReciter, surah: Int? = null): Boolean {
        synchronized(this) { if (busy) return false; busy = true }
        return try {
            withContext(Dispatchers.Main) { QuranPlaybackService.pauseForMaintenance() }
            withContext(Dispatchers.IO) {
                ensureActive()
                val store = QuranAudioStore(context, reciter)
                if (surah == null) store.deleteAll() else store.deleteSurah(surah)
            }
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: java.io.IOException) {
            false
        } finally {
            synchronized(this) { busy = false; mutableRevision.value++ }
        }
    }

    companion object {
        @Volatile private var instance: QuranAudioLibrary? = null
        fun get(context: Context): QuranAudioLibrary = instance ?: synchronized(this) {
            instance ?: QuranAudioLibrary(context.applicationContext).also { instance = it }
        }
        internal fun createForTest(context: Context, fetcher: AudioFetcher): QuranAudioLibrary = QuranAudioLibrary(context, fetcher)
        fun requiredBytes(reciter: QuranReciter, remaining: Int): Long = estimatedBytes(reciter) * remaining / 6236 + 10L * 1024 * 1024
        fun estimatedBytes(reciter: QuranReciter): Long = 30L * 60 * 60 * reciter.bitrateKbps * 1000 / 8
    }
}
