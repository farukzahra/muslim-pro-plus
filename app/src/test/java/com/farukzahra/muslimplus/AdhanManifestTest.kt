package com.farukzahra.muslimplus

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/** O servico do adhan precisa existir com tipo mediaPlayback e nao exportado. */
class AdhanManifestTest {
    private val manifest = File("src/main/AndroidManifest.xml")
    private val androidNs = "http://schemas.android.com/apk/res/android"

    @Test
    fun adhanServiceIsDeclaredAsMediaPlaybackAndNotExported() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)
        val services = document.getElementsByTagName("service")
        val adhan = (0 until services.length)
            .map { services.item(it) as Element }
            .firstOrNull { it.getAttributeNS(androidNs, "name") == ".adhan.AdhanService" }

        assertTrue("servico do adhan ausente no AndroidManifest", adhan != null)
        assertEquals("false", adhan!!.getAttributeNS(androidNs, "exported"))
        assertEquals("mediaPlayback", adhan.getAttributeNS(androidNs, "foregroundServiceType"))
    }

    @Test
    fun permissionsForForegroundPlaybackAndWakeLockAreDeclared() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)
        val uses = document.getElementsByTagName("uses-permission")
        val names = (0 until uses.length)
            .map { (uses.item(it) as Element).getAttributeNS(androidNs, "name") }
            .toSet()

        assertTrue("android.permission.WAKE_LOCK" in names)
        assertTrue("android.permission.FOREGROUND_SERVICE" in names)
        assertTrue("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" in names)
    }
}
