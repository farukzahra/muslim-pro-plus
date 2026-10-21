package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.ArabicText
import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicTextTest {
    @Test
    fun removesHarakat() {
        assertEquals("بسم الله", ArabicText.normalize("بِسْمِ اللَّهِ"))
    }

    @Test
    fun unifiesAlefVariants() {
        assertEquals("انزل", ArabicText.normalize("أنزل"))
        assertEquals("ايه", ArabicText.normalize("آية"))
    }

    @Test
    fun unifiesYaAndTaMarbuta() {
        assertEquals("هدي", ArabicText.normalize("هدى"))
        assertEquals("صلاه", ArabicText.normalize("صلاة"))
    }
}
