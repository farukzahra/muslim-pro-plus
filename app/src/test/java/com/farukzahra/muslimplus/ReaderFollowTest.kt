package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.ui.screens.quran.ReaderFollow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReaderFollowTest {
    private val verses = (1..7).map { Verse(surah = 1, ayah = it, text = "") }

    @Test
    fun followsThePlayingVerseOfTheOpenSurah() {
        assertEquals(0, ReaderFollow.targetIndex(verses, 1, 1, 1, true))
        assertEquals(4, ReaderFollow.targetIndex(verses, 1, 1, 5, true))
    }

    @Test
    fun doesNothingWhenAudioIsPaused() {
        assertNull(ReaderFollow.targetIndex(verses, 1, 1, 3, false))
    }

    @Test
    fun ignoresAudioFromAnotherSurah() {
        assertNull(ReaderFollow.targetIndex(verses, 1, 2, 3, true))
    }

    @Test
    fun ignoresAnAyahThatIsNotInTheList() {
        assertNull(ReaderFollow.targetIndex(verses, 1, 1, 99, true))
        assertNull(ReaderFollow.targetIndex(verses, null, 1, 3, true))
        assertNull(ReaderFollow.targetIndex(verses, 1, 1, null, true))
    }
}
