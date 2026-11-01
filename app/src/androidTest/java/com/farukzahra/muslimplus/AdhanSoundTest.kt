package com.farukzahra.muslimplus

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdhanSoundTest {
    @Test
    fun hasTwentyTwoVoicesWithUniqueIdsAndPlayableResources() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals(22, AdhanSound.entries.size)
        assertEquals(22, AdhanSound.entries.map { it.id }.toSet().size)
        assertEquals(16, AdhanSound.normal.size)
        assertEquals(6, AdhanSound.fajr.size)

        AdhanSound.entries.forEach { sound ->
            val bytes = context.resources.openRawResource(sound.resId).use { it.readBytes() }
            assertTrue("audio vazio: ${sound.id}", bytes.isNotEmpty())
            assertTrue("duracao invalida: ${sound.id}", sound.durationSeconds > 30)
        }
    }

    @Test
    fun fajrVoicesAreMarkedAndTheDefaultFajrIsOneOfThem() {
        assertTrue(AdhanSound.defaultFajr.fajrOnly)
        assertTrue(AdhanSound.defaultFajr in AdhanSound.fajr)
        AdhanSound.fajr.forEach { sound ->
            assertTrue("voz de Fajr sem tathwib: ${sound.id}", sound.fajrOnly)
        }
        AdhanSound.normal.forEach { sound ->
            assertTrue("voz comum marcada como Fajr: ${sound.id}", !sound.fajrOnly)
        }
    }

    @Test
    fun everyVoiceWithACountryHasAFlag() {
        AdhanSound.entries.forEach { sound ->
            val country = sound.country
            if (country != null) {
                assertTrue("bandeira invalida: ${sound.id}", country.flagRes != 0)
            }
        }
        assertNull(AdhanSound.CLASSIC.country)
    }

    @Test
    fun findNormalAndFindFajrDoNotCrossGroups() {
        assertEquals(AdhanSound.CLASSIC, AdhanSound.findNormal(AdhanSound.FAJR_MAKKAH.id))
        assertEquals(AdhanSound.defaultFajr, AdhanSound.findFajr(AdhanSound.CLASSIC.id))
        assertEquals(AdhanSound.MAKKAH_MULLA, AdhanSound.findNormal(AdhanSound.MAKKAH_MULLA.id))
        assertEquals(AdhanSound.FAJR_CAIRO, AdhanSound.findFajr(AdhanSound.FAJR_CAIRO.id))
    }
}
