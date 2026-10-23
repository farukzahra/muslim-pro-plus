package com.farukzahra.muslimplus

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.quran.audio.QuranStreamingCache
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import androidx.media3.datasource.DefaultDataSource
import java.io.IOException
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@UnstableApi
@RunWith(AndroidJUnit4::class)
class QuranStreamingCacheTest {
    @Test
    fun secondPlaybackUsesCacheAndClearPreservesExplicitDownloads() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        QuranStreamingCache.clear(context)
        val payload = ByteArray(32_000) { (it % 100).toByte() }
        var upstreamOpens = 0
        val upstream = DataSource.Factory {
            val bytes = ByteArrayDataSource(payload)
            object : DataSource by bytes {
                override fun open(dataSpec: DataSpec): Long {
                    upstreamOpens++
                    return bytes.open(dataSpec)
                }
            }
        }
        val factory = QuranStreamingCache.factory(context, upstream)
        fun read(uri: String): Int {
            val source = factory.createDataSource()
            return try {
                source.open(DataSpec(Uri.parse(uri)))
                val buffer = ByteArray(4096)
                var total = 0
                while (true) {
                    val count = source.read(buffer, 0, buffer.size)
                    if (count == -1) break
                    total += count
                }
                total
            } finally { source.close() }
        }
        val voiceA = "https://cache-test.invalid/alafasy/001001.mp3"
        val voiceB = "https://cache-test.invalid/husary/001001.mp3"
        assertEquals(payload.size, read(voiceA))
        val opensAfterFirst = upstreamOpens
        assertEquals(payload.size, read(voiceA))
        assertEquals(opensAfterFirst, upstreamOpens)
        assertEquals(payload.size, read(voiceB))
        assertTrue(upstreamOpens > opensAfterFirst)
        assertTrue(QuranStreamingCache.bytes(context) > 0)
        val offline = File(context.filesDir, "audio/cache-test/001/001001.mp3")
        offline.parentFile!!.mkdirs()
        offline.writeBytes(payload)
        try {
            QuranStreamingCache.clear(context)
            assertEquals(0L, QuranStreamingCache.bytes(context))
            assertEquals(payload.size.toLong(), offline.length())
        } finally { offline.delete(); offline.parentFile?.delete(); offline.parentFile?.parentFile?.delete() }
    }

    @Test
    fun existingQueueResolvesDownloadAndDeletionAtEachOpen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val voice = QuranReciters.default
        val store = QuranAudioStore(context, voice)
        val file = store.fileFor(114, 6)
        val previous = file.takeIf { it.exists() }?.readBytes()
        file.delete()
        QuranStreamingCache.clear(context)
        var networkAllowed = true
        var networkOpens = 0
        val remote = DataSource.Factory {
            val data = ByteArrayDataSource(ByteArray(100) { 1 })
            object : DataSource by data {
                override fun open(dataSpec: DataSpec): Long {
                    networkOpens++
                    if (!networkAllowed) throw IOException("Offline test")
                    return data.open(dataSpec)
                }
            }
        }
        val factory = QuranStreamingCache.factory(context, DefaultDataSource.Factory(context, remote))
        val queuedSpec = DataSpec(Uri.parse(voice.urlFor(114, 6)))
        fun read(): Int {
            val data = factory.createDataSource()
            return try {
                data.open(queuedSpec)
                var total = 0
                val buffer = ByteArray(1024)
                while (true) {
                    val count = data.read(buffer, 0, buffer.size)
                    if (count == -1) break
                    total += count
                }
                total
            } finally { data.close() }
        }
        try {
            assertEquals(100, read())
            QuranStreamingCache.clear(context)
            store.prepareDir(114)
            file.writeBytes(ByteArray(200) { 2 })
            networkAllowed = false
            assertEquals(200, read())
            assertEquals(1, networkOpens)
            // Arquivo baixado abre direto, sem criar uma segunda cópia no cache.
            assertEquals(0L, QuranStreamingCache.bytes(context))
            file.delete()
            QuranStreamingCache.clear(context)
            networkAllowed = true
            assertEquals(100, read())
            assertEquals(2, networkOpens)
        } finally {
            if (previous == null) file.delete() else file.writeBytes(previous)
            QuranStreamingCache.clear(context)
        }
    }
}
