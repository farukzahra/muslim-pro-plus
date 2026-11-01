package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.adhan.AdhanMigrationPlan
import com.farukzahra.muslimplus.adhan.AdhanSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdhanMigrationTest {
    @Test
    fun fajrSoundAsGlobalMovesToFajrAndShowsTheNotice() {
        val plan = AdhanMigrationPlan.plan(AdhanSound.MADINAH_NAFMAN.id, null)
        assertEquals(AdhanSound.default.id, plan.globalId)
        assertEquals(AdhanSound.MADINAH_NAFMAN.id, plan.fajrId)
        assertEquals(true, plan.override)
        assertEquals(true, plan.showNotice)
    }

    @Test
    fun unknownGlobalFallsBackToClassicWithoutNotice() {
        val plan = AdhanMigrationPlan.plan("legacy-voice", null)
        assertEquals(AdhanSound.default.id, plan.globalId)
        assertNull(plan.fajrId)
        assertNull(plan.override)
        assertEquals(false, plan.showNotice)
    }

    @Test
    fun normalFajrChoiceFallsBackToTheDefaultFajr() {
        val plan = AdhanMigrationPlan.plan(AdhanSound.CLASSIC.id, AdhanSound.MAKKAH_MULLA.id)
        assertNull(plan.globalId)
        assertEquals(AdhanSound.defaultFajr.id, plan.fajrId)
        assertNull(plan.override)
        assertEquals(false, plan.showNotice)
    }

    @Test
    fun validConfigurationIsLeftAlone() {
        val plan = AdhanMigrationPlan.plan(AdhanSound.CLASSIC.id, AdhanSound.FAJR_MAKKAH.id)
        assertNull(plan.globalId)
        assertNull(plan.fajrId)
        assertNull(plan.override)
        assertEquals(false, plan.showNotice)
    }
}
