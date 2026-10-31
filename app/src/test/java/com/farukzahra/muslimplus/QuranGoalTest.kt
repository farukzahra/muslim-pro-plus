package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.QuranGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranGoalTest {
    @Test
    fun onlySupportedGoalsAreAccepted() {
        assertEquals(QuranGoal.DEFAULT_MINUTES, QuranGoal.sanitize(null))
        assertEquals(QuranGoal.DEFAULT_MINUTES, QuranGoal.sanitize(7))
        assertEquals(5, QuranGoal.sanitize(5))
        assertEquals(30, QuranGoal.sanitize(30))
    }

    @Test
    fun khatamEstimateFollowsTheChosenGoal() {
        // 5 min/dia ~ 30 ayahs: 6236/30 = 207.9 -> 208 dias.
        assertEquals(208, QuranGoal.khatamDays(5))
        assertEquals(104, QuranGoal.khatamDays(10))
        assertEquals(70, QuranGoal.khatamDays(15))
        assertEquals(35, QuranGoal.khatamDays(30))
        assertTrue(QuranGoal.khatamDays(30) < QuranGoal.khatamDays(5))
    }
}
