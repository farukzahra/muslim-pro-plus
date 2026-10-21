package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.settings.Licenses
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrato da tela de Licencas. A lista e dado puro ([Licenses.all]), entao o
 * teste roda em JVM sem Compose e quebra o build quando alguem adiciona uma
 * atribuicao sem licenca ou com link inseguro.
 */
class LicensesTest {
    @Test
    fun listIsNotEmpty() {
        assertTrue("Licenses.all esta vazio", Licenses.all.isNotEmpty())
    }

    @Test
    fun everyEntryHasNameLicenseAndHttpsUrl() {
        Licenses.all.forEach { entry ->
            assertTrue("entrada sem nome: $entry", entry.name.isNotBlank())
            assertTrue("${entry.name} sem licenca", entry.license.isNotBlank())
            assertTrue("${entry.name} sem URL", entry.url.isNotBlank())
            assertTrue(
                "${entry.name} usa URL fora de https://: ${entry.url}",
                entry.url.startsWith("https://"),
            )
        }
    }

    @Test
    fun entriesAreIdentifiableByAName() {
        val names = Licenses.all.map { it.name }
        assertEquals("nome repetido em Licenses.all", names.size, names.toSet().size)
    }

    /** Gate de release: estas atribuicoes nao podem sumir da lista. */
    @Test
    fun requiredAttributionsArePresent() {
        val names = Licenses.all.map { it.name }.toSet()
        listOf(
            "Tanzil",
            "GeoNames",
            "adhan-java",
            "AndroidX Media3",
            "everyayah.com",
            "Helmi Nasr",
            "Pickthall",
        ).forEach { required ->
            assertTrue("falta a atribuicao de $required", required in names)
        }
    }
}
