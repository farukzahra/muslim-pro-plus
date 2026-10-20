package com.farukzahra.muslimplus

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Garante que os tres locales tem exatamente as mesmas chaves e que nenhuma
 * string ficou vazia. Roda como teste JVM e quebra o build quando alguem
 * adiciona uma string so no ingles.
 */
class LocaleParityTest {
    private val resDir = File("src/main/res")
    private val locales = listOf("values", "values-pt-rBR", "values-ar")

    private fun stringsOf(dir: String): Map<String, String> {
        val file = File(resDir, "$dir/strings.xml")
        if (!file.exists()) return emptyMap()
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val element = nodes.item(index) as Element
            element.getAttribute("name") to element.textContent
        }
    }

    @Test
    fun everyLocaleHasTheSameKeys() {
        val base = stringsOf("values")
        assertTrue("values/strings.xml esta vazio", base.isNotEmpty())
        locales.forEach { locale ->
            assertEquals("chaves divergentes em $locale", base.keys, stringsOf(locale).keys)
        }
    }

    @Test
    fun noLocaleHasAnEmptyString() {
        locales.forEach { locale ->
            stringsOf(locale).forEach { (key, value) ->
                assertTrue("$locale/$key esta vazio", value.isNotBlank())
            }
        }
    }
}
