package com.farukzahra.muslimplus

import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.audio.*
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranAudioLibraryTest {
    private class TestContext(base: Context) : ContextWrapper(base) {
        val directory = File(base.cacheDir, "library-test-${System.nanoTime()}").apply { mkdirs() }
        var failFiles = false
        override fun getFilesDir(): File { if (failFiles) throw IOException("test disk failure"); return directory }
    }
    private val surah = Surah(1, "", "", 2)

    @Test fun cancellationHoldsWriterUntilFetchUnwindsAndRetrySkipsCompletedVerse() = runBlocking {
        val context = TestContext(ApplicationProvider.getApplicationContext())
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger()
        val voice = QuranReciters.default
        val store = QuranAudioStore(context, voice)
        store.prepareDir(1)
        store.fileFor(1, 1).writeBytes(byteArrayOf(9))
        val library = QuranAudioLibrary.createForTest(context, AudioFetcher { _, destination ->
            if (calls.incrementAndGet() == 1) {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
            }
            destination.writeBytes(byteArrayOf(1, 2, 3))
        })
        try {
            library.download(voice, listOf(surah))
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            library.cancel()
            library.download(QuranReciters.all[1], listOf(surah))
            assertTrue(library.progress.value.running)
            assertEquals(voice.id, library.progress.value.reciterId)
            assertFalse(library.delete(voice))
            release.countDown()
            withTimeout(5000) { library.progress.first { !it.running } }
            assertFalse(store.partFor(1, 2).exists())
            assertNull(store.localAudio(1, 2))
            assertArrayEquals(byteArrayOf(9), store.localAudio(1, 1)!!.readBytes())
            library.download(voice, listOf(surah))
            withTimeout(5000) { library.progress.first { !it.running } }
            assertEquals(2, calls.get())
            assertTrue(store.isSurahSaved(1, 2))
            assertFalse(library.progress.value.failed)
            assertTrue(library.delete(voice, 1))
            assertEquals(0, store.savedVerses(1, 2))
        } finally {
            release.countDown()
            library.cancel()
            withTimeout(5000) { library.progress.first { !it.running } }
            context.directory.deleteRecursively()
        }
    }

    @Test fun deletionIoFailureReturnsFalseAndReleasesWriter() = runBlocking {
        val context = TestContext(ApplicationProvider.getApplicationContext())
        val library = QuranAudioLibrary.createForTest(context, AudioFetcher { _, _ -> error("No network") })
        try {
            context.failFiles = true
            assertFalse(library.delete(QuranReciters.default))
            context.failFiles = false
            assertTrue(library.delete(QuranReciters.default))
        } finally { context.directory.deleteRecursively() }
    }
}
