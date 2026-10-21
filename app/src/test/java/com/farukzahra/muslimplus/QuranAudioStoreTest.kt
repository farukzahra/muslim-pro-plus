package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.EveryAyahSource
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Cache em disco: nomes, checagem da sura inteira e LRU por tamanho. */
class QuranAudioStoreTest {
    @get:Rule
    val temp = TemporaryFolder()

    private fun store(limitBytes: Long = Long.MAX_VALUE) =
        QuranAudioStore(temp.root, EveryAyahSource, limitBytes)

    private fun write(file: File, bytes: Int, lastModified: Long) {
        file.parentFile?.mkdirs()
        file.writeBytes(ByteArray(bytes))
        file.setLastModified(lastModified)
    }

    @Test
    fun filesLiveUnderTheSurahFolderWithTheCdnName() {
        val store = store()
        assertEquals(
            File(temp.root, "002/002255.mp3").absolutePath,
            store.fileFor(2, 255).absolutePath,
        )
    }

    @Test
    fun surahIsSavedOnlyWhenEveryVerseExists() {
        val store = store()
        write(store.fileFor(1, 1), 10, 1)
        write(store.fileFor(1, 2), 10, 1)
        assertFalse(store.isSurahSaved(1, 3))
        write(store.fileFor(1, 3), 10, 1)
        assertTrue(store.isSurahSaved(1, 3))
        assertFalse(store.isSurahSaved(2, 1))
    }

    @Test
    fun evictDeletesTheOldestFileWhenOverTheLimit() {
        val store = store(limitBytes = 100)
        write(store.fileFor(1, 1), 60, 1_000)
        write(store.fileFor(2, 1), 60, 2_000)

        val deleted = store.evict()

        assertEquals(listOf(store.fileFor(1, 1).absolutePath), deleted)
        assertFalse(store.fileFor(1, 1).exists())
        assertTrue(store.fileFor(2, 1).exists())
    }

    @Test
    fun evictNeverTouchesTheSurahBeingPlayed() {
        val store = store(limitBytes = 50)
        write(store.fileFor(1, 1), 60, 1_000)
        write(store.fileFor(2, 1), 60, 2_000)

        val deleted = store.evict(keepSurah = 1)

        assertEquals(listOf(store.fileFor(2, 1).absolutePath), deleted)
        assertTrue(store.fileFor(1, 1).exists())
        assertFalse(store.fileFor(2, 1).exists())
    }

    @Test
    fun touchMakesAVerseTheMostRecent() {
        val store = store(limitBytes = 100)
        write(store.fileFor(1, 1), 60, 1_000)
        write(store.fileFor(2, 1), 60, 2_000)

        store.touch(1, 1)
        val deleted = store.evict()

        assertEquals(listOf(store.fileFor(2, 1).absolutePath), deleted)
    }

    @Test
    fun partialDownloadsAreNotConsideredCached() {
        val store = store()
        write(store.partFor(1, 1), 10, 1)
        assertFalse(store.isSurahSaved(1, 1))
    }
}
