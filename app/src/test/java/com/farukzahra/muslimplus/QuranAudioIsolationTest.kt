package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.*
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class QuranAudioIsolationTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun voicesUseSeparateRootsAndDeletionPreservesOthers() {
        val a = QuranAudioStore(temp.root, QuranReciters.default)
        val b = QuranAudioStore(temp.root, QuranReciters.all[1])
        a.prepareDir(1); b.prepareDir(1)
        a.fileFor(1,1).writeText("a"); b.fileFor(1,1).writeText("bb")
        a.partFor(1,2).writeText("partial")
        assertEquals(1, a.savedVerses(1,7))
        assertEquals(1L, a.totalBytes())
        a.deleteSurah(1)
        assertNotNull(b.localAudio(1,1))
        b.deleteAll()
        assertNull(b.localAudio(1,1))
    }
    @Test fun legacyBelongsOnlyToDefaultAndIsMigrated() {
        val legacy = File(temp.root,"001/001001.mp3")
        legacy.parentFile!!.mkdirs(); legacy.writeText("old")
        val other = QuranAudioStore(temp.root,QuranReciters.all[1])
        assertNull(other.localAudio(1,1))
        val default = QuranAudioStore(temp.root,QuranReciters.default)
        assertEquals("old",default.localAudio(1,1)!!.readText())
        assertFalse(legacy.exists())
        assertTrue(default.fileFor(1,1).path.contains("alafasy-128"))
    }
    @Test fun catalogHasSafeStableDistinctIdsAndValidVerseUrls() {
        assertEquals("alafasy-128", QuranReciters.default.id)
        assertTrue(QuranReciters.all.size >= 4)
        assertEquals(QuranReciters.all.size,QuranReciters.all.map { it.id }.toSet().size)
        assertNull(QuranReciters.find("../../other"))
        QuranReciters.all.forEach {
            assertTrue(it.urlFor(9,1).endsWith("009001.mp3"))
            assertTrue(it.urlFor(114,6).endsWith("114006.mp3"))
            assertThrows(IllegalArgumentException::class.java) { it.urlFor(115,1) }
            assertThrows(IllegalArgumentException::class.java) { it.urlFor(114,7) }
            assertThrows(IllegalArgumentException::class.java) { it.urlFor(1,287) }
        }
    }
}

