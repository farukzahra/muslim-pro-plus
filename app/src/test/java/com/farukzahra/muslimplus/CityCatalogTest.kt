package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.location.CityCatalog
import java.io.File
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O asset e' gerado por `tools/cities/build_cities.py`. Sem `Context` no teste
 * JVM, o catalogo e' montado a partir das mesmas linhas do asset.
 */
class CityCatalogTest {
    private val asset = File("src/main/assets/cities.txt")
    private val lines = asset.readLines(Charsets.UTF_8).filter { it.isNotBlank() }
    private val catalog = CityCatalog.fromLines(lines)

    @Test
    fun assetExistsWithExpectedSize() {
        assertTrue(asset.isFile)
        assertTrue("${lines.size} linhas", lines.size in 1000..3000)
    }

    @Test
    fun everyLineIsParsed() {
        assertEquals(lines.size, catalog.all().size)
    }

    @Test
    fun catalogIsSortedByPopulation() {
        val populations = catalog.all().map { it.population }
        assertEquals(populations.sortedDescending(), populations)
        assertTrue(catalog.first().population > 1_000_000)
    }

    @Test
    fun byIdOfFirstLineReturnsIt() {
        val first = catalog.all().first()
        assertEquals(first, catalog.byId(first.id))
    }

    @Test
    fun unknownIdFallsBackToFirstCity() {
        assertEquals(catalog.first(), catalog.byId("nao-existe"))
        assertNull(catalog.byIdOrNull("nao-existe"))
    }

    @Test
    fun everyCityHasUsableZoneAndCountry() {
        catalog.all().forEach { city ->
            // ZoneId.of na ViewModel: fuso invalido derrubaria a tela.
            assertNotNull("fuso de ${city.name}", ZoneId.of(city.zoneId))
            assertEquals("pais de ${city.name}", 2, city.countryCode.length)
        }
    }

    @Test
    fun defaultForZoneOfSaoPauloIsSaoPaulo() {
        val city = catalog.defaultForZone("America/Sao_Paulo")
        assertNotNull(city)
        assertEquals("São Paulo", city?.name)
        assertEquals("BR", city?.countryCode)
    }

    @Test
    fun zoneWithoutCitiesHasNoDefault() {
        assertNull(catalog.defaultForZone("Nowhere/Nothing"))
    }

    @Test
    fun searchFindsLondon() {
        assertTrue(catalog.search("lond").any { it.name == "London" })
        assertTrue(catalog.search("LONDON").any { it.name == "London" })
    }

    @Test
    fun searchIgnoresAccents() {
        assertTrue(catalog.search("sao paulo").any { it.name == "São Paulo" })
        assertTrue(catalog.search("brasilia").any { it.countryCode == "BR" })
    }

    @Test
    fun emptySearchReturnsTheFirstSixty() {
        assertEquals(CityCatalog.MAX_RESULTS, catalog.search("").size)
        assertEquals(catalog.all().take(CityCatalog.MAX_RESULTS), catalog.search(" "))
    }

    @Test
    fun searchWithoutMatchesIsEmpty() {
        assertTrue(catalog.search("zzzzzzqqqq").isEmpty())
    }

    @Test
    fun nearestOfSaoPauloReturnsSaoPaulo() {
        // Coordenada de GPS no centro da cidade, nao o ponto exato do catalogo.
        val city = catalog.nearest(-23.55, -46.63)
        assertEquals("São Paulo", city.name)
        assertEquals("BR", city.countryCode)
        assertEquals("America/Sao_Paulo", city.zoneId)
    }

    @Test
    fun nearestOfRioReturnsRio() {
        val city = catalog.nearest(-22.9068, -43.1729)
        assertEquals("Rio de Janeiro", city.name)
    }

    @Test
    fun haversineMatchesKnownDistance() {
        // Sao Paulo -> Rio de Janeiro: ~357 km em linha reta.
        val distance = CityCatalog.haversineKm(-23.5505, -46.6333, -22.9068, -43.1729)
        assertTrue("$distance km", distance in 340.0..380.0)
    }

    @Test
    fun nearestInOpenOceanStillReturnsAValidCity() {
        // Meio do Pacifico Sul: nao ha "sem resultado", o app precisa de um fuso.
        val city = catalog.nearest(-40.0, -140.0)
        assertTrue(catalog.all().any { it.id == city.id })
        assertNotNull("fuso de ${city.name}", ZoneId.of(city.zoneId))
        // Deve ser a mais proxima mesmo: nada de cair na primeira do catalogo.
        val distance = CityCatalog.haversineKm(-40.0, -140.0, city.latitude, city.longitude)
        assertTrue("$distance km de ${city.name}", distance > 100.0)
        val closest = catalog.all().minOf {
            CityCatalog.haversineKm(-40.0, -140.0, it.latitude, it.longitude)
        }
        assertEquals(closest, distance, 0.001)
    }
}
