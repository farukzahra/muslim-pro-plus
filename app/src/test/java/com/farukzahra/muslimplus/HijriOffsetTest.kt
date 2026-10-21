package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.HijriOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class HijriOffsetTest {
    @Test
    fun acceptsOnlyMinusOneZeroAndPlusOne() {
        listOf(-1, 0, 1).forEach { days ->
            assertEquals(days, HijriOffset.sanitize(days))
        }
    }

    @Test
    fun anythingElseFallsBackToZero() {
        listOf(-2, 2, -365, 365, Int.MIN_VALUE, Int.MAX_VALUE).forEach { days ->
            assertEquals("valor $days", HijriOffset.DEFAULT, HijriOffset.sanitize(days))
        }
    }
}
