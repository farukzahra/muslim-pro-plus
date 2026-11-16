package com.farukzahra.muslimplus

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLinksTest {
    @Test
    fun playUrlPointsToThisPackage() {
        assertEquals("com.farukzahra.muslimplus", AppLinks.PLAY_URL.substringAfter("id="))
        assertEquals(
            "https://play.google.com/store/apps/details",
            AppLinks.PLAY_URL.substringBefore("?id="),
        )
    }

    @Test
    fun contactEmailIsTheProjectAddress() {
        assertEquals("farukz@gmail.com", AppLinks.CONTACT_EMAIL)
    }
}
