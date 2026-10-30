package com.farukzahra.muslimplus

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.adhan.AdhanSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdhanSoundTest {
    @Test
    fun hasTenVoicesWithUniqueIdsAndPlayableResources() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals(10, AdhanSound.entries.size)
        assertEquals(10, AdhanSound.entries.map { it.id }.toSet().size)
        AdhanSound.entries.forEach { sound ->
            val bytes = context.resources.openRawResource(sound.resId).use { it.readBytes() }
            assertTrue("audio vazio: ${sound.id}", bytes.isNotEmpty())
        }
    }
}
