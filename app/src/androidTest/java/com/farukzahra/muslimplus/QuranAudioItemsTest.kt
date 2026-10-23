package com.farukzahra.muslimplus

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.quran.Surah
import com.farukzahra.muslimplus.quran.Verse
import com.farukzahra.muslimplus.quran.audio.QuranAudioItems
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranAudioItemsTest {
    @Test fun queueCarriesSelectedVoiceAndRemoteUri() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val surah = Surah(1, "الفاتحة", "Al-Fatihah", 7)
        val verses = listOf(Verse(1, 1, "x"), Verse(1, 2, "y"))
        val voice = QuranReciters.all[1]
        val items = QuranAudioItems.forSurah(context, surah, verses, voice)
        assertEquals(verses.size, items.size)
        items.forEachIndexed { index, item ->
            val ayah = index + 1
            assertEquals(voice.urlFor(1, ayah), item.localConfiguration?.uri.toString())
            assertEquals(voice.reciter, item.mediaMetadata.artist)
            assertEquals(voice.id, item.mediaMetadata.extras?.getString("reciter_id"))
        }
    }
}
