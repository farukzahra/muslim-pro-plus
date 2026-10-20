package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.ui.nav.Tab
import org.junit.Assert.assertEquals
import org.junit.Test

class TabTest {
    @Test
    fun hasFiveTabs() {
        assertEquals(5, Tab.entries.size)
    }

    @Test
    fun routesAreStable() {
        assertEquals(
            listOf("today", "prayers", "quran", "qibla", "more"),
            Tab.entries.map { it.route },
        )
    }
}
