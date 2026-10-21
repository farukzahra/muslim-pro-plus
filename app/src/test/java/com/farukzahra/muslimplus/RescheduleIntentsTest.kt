package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.notifications.RescheduleActions
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * O reagendamento so acontece para as acoes listadas em [RescheduleActions] e
 * o manifesto precisa declarar exatamente essas acoes. Um erro de digitacao em
 * qualquer um dos dois lados deixaria o alarme na hora antiga depois de trocar
 * fuso, mudar a hora ou atualizar o app.
 */
class RescheduleIntentsTest {
    private val manifest = File("src/main/AndroidManifest.xml")
    private val androidNs = "http://schemas.android.com/apk/res/android"

    @Test
    fun handlesBootCompleted() {
        assertTrue(RescheduleActions.handles("android.intent.action.BOOT_COMPLETED"))
    }

    @Test
    fun handlesTimezoneChanged() {
        assertTrue(RescheduleActions.handles("android.intent.action.TIMEZONE_CHANGED"))
    }

    @Test
    fun handlesTimeSet() {
        assertTrue(RescheduleActions.handles("android.intent.action.TIME_SET"))
    }

    @Test
    fun handlesPackageReplaced() {
        assertTrue(RescheduleActions.handles("android.intent.action.MY_PACKAGE_REPLACED"))
    }

    @Test
    fun ignoresUnknownAction() {
        assertFalse(RescheduleActions.handles("android.intent.action.BATTERY_LOW"))
        assertFalse(RescheduleActions.handles(""))
        assertFalse(RescheduleActions.handles(null))
    }

    @Test
    fun constantsMatchPlatformActionNames() {
        assertEquals("android.intent.action.BOOT_COMPLETED", RescheduleActions.BOOT_COMPLETED)
        assertEquals("android.intent.action.TIMEZONE_CHANGED", RescheduleActions.TIMEZONE_CHANGED)
        assertEquals("android.intent.action.TIME_SET", RescheduleActions.TIME_SET)
        assertEquals("android.intent.action.MY_PACKAGE_REPLACED", RescheduleActions.MY_PACKAGE_REPLACED)
    }

    @Test
    fun manifestDeclaresEveryHandledAction() {
        val declared = actionsByExportedFlag().keys
        val expected = setOf(
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.TIMEZONE_CHANGED",
            "android.intent.action.TIME_SET",
            "android.intent.action.MY_PACKAGE_REPLACED",
        )
        expected.forEach { action ->
            assertTrue("acao ausente no AndroidManifest: $action", action in declared)
        }
    }

    @Test
    fun onlyBootReceiverIsExported() {
        val exported = actionsByExportedFlag()
        assertEquals(
            "true",
            exported["android.intent.action.BOOT_COMPLETED"],
        )
        listOf(
            "android.intent.action.TIMEZONE_CHANGED",
            "android.intent.action.TIME_SET",
            "android.intent.action.MY_PACKAGE_REPLACED",
        ).forEach { action ->
            assertEquals("$action deve vir de receiver exported=false", "false", exported[action])
        }
    }

    /** Mapa acao declarada -> valor de android:exported do receiver que a recebe. */
    private fun actionsByExportedFlag(): Map<String, String> {
        assertTrue("AndroidManifest nao encontrado em ${manifest.absolutePath}", manifest.isFile)
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(manifest)
        val result = mutableMapOf<String, String>()
        val receivers = document.getElementsByTagName("receiver")
        for (index in 0 until receivers.length) {
            val receiver = receivers.item(index) as Element
            val exported = receiver.getAttributeNS(androidNs, "exported")
            val actions = receiver.getElementsByTagName("action")
            for (actionIndex in 0 until actions.length) {
                val name = (actions.item(actionIndex) as Element).getAttributeNS(androidNs, "name")
                if (name.isNotEmpty()) result[name] = exported
            }
        }
        return result
    }
}
