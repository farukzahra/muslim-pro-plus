package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.AudioFetcher
import com.farukzahra.muslimplus.quran.audio.EveryAyahSource
import com.farukzahra.muslimplus.quran.audio.QuranAudioDownloader
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import java.io.File
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Download sem internet: o [AudioFetcher] e fake e escreve bytes locais. Assim
 * o teste cobre nomes, progresso, retomada e `.part` sem depender de CDN.
 */
class QuranAudioDownloaderTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val fetcher = FakeFetcher()

    private fun downloader(store: QuranAudioStore) =
        QuranAudioDownloader(store, EveryAyahSource, fetcher)

    @Test
    fun downloadsEveryVerseAndReportsProgress() = runTest {
        val store = QuranAudioStore(temp.root, EveryAyahSource)
        val progress = mutableListOf<Int>()

        val result = downloader(store).download(1, 3) { done, total ->
            assertEquals(3, total)
            progress += done
        }

        assertEquals(3, result.downloaded)
        assertEquals(0, result.failed)
        assertEquals(listOf(1, 2, 3), progress)
        assertEquals(
            listOf(
                "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
                "https://everyayah.com/data/Alafasy_128kbps/001002.mp3",
                "https://everyayah.com/data/Alafasy_128kbps/001003.mp3",
            ),
            fetcher.requested,
        )
        assertTrue(store.isSurahSaved(1, 3))
        assertFalse(store.partFor(1, 1).exists())
    }

    @Test
    fun secondRunSkipsVersesAlreadyOnDisk() = runTest {
        val store = QuranAudioStore(temp.root, EveryAyahSource)
        downloader(store).download(1, 2)
        fetcher.requested.clear()

        val result = downloader(store).download(1, 2)

        assertEquals(2, result.downloaded)
        assertTrue(fetcher.requested.isEmpty())
    }

    @Test
    fun failedVerseIsCountedAndLeavesNoPartialFile() = runTest {
        val store = QuranAudioStore(temp.root, EveryAyahSource)
        fetcher.failFileName = "001002.mp3"

        val result = downloader(store).download(1, 3)

        assertEquals(2, result.downloaded)
        assertEquals(1, result.failed)
        assertFalse(result.isComplete)
        assertFalse(store.partFor(1, 2).exists())
        assertFalse(store.fileFor(1, 2).exists())
        assertTrue(store.localAudio(1, 1) != null)
        assertTrue(store.localAudio(1, 3) != null)
    }

    private class FakeFetcher : AudioFetcher {
        val requested = mutableListOf<String>()
        var failFileName: String? = null

        override fun fetch(url: String, destination: File) {
            requested += url
            if (failFileName != null && url.endsWith(failFileName!!)) {
                throw IOException("fake failure")
            }
            destination.writeBytes(ByteArray(16))
        }
    }
}
