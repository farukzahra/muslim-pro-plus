package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.location.LocationRepository
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Escopo de localizacao exigido pelo aviso de politicas do Play de outubro/2026:
 * o app so precisa da cidade mais proxima, entao pede apenas localizacao
 * aproximada e nao declara permissao restrita fora do escopo auditado.
 */
class LocationScopeTest {
    private val manifest = File("src/main/AndroidManifest.xml")
    private val androidNs = "http://schemas.android.com/apk/res/android"

    private fun permissionNames(): Set<String> {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)
        val uses = document.getElementsByTagName("uses-permission")
        return (0 until uses.length)
            .map { (uses.item(it) as Element).getAttributeNS(androidNs, "name") }
            .toSet()
    }

    @Test
    fun manifestDeclaresOnlyCoarseLocation() {
        val names = permissionNames()
        assertTrue(
            "falta android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION" in names,
        )
        assertFalse(
            "ACCESS_FINE_LOCATION nao se justifica: a cidade mais proxima basta",
            "android.permission.ACCESS_FINE_LOCATION" in names,
        )
    }

    @Test
    fun manifestHasNoRestrictedPermissionOutsideTheAudit() {
        val restricted = setOf(
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
        )
        val declared = permissionNames().intersect(restricted)
        assertTrue("permissoes restritas no manifesto: $declared", declared.isEmpty())
    }

    @Test
    fun repositoryAsksForCoarseLocationOnly() {
        assertEquals(
            listOf("android.permission.ACCESS_COARSE_LOCATION"),
            LocationRepository.PERMISSIONS.toList(),
        )
    }
}
