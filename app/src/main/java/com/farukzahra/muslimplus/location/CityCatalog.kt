package com.farukzahra.muslimplus.location

import android.content.Context
import java.text.Normalizer
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Cidade do catalogo GeoNames (cities5000), ordenada por populacao decrescente.
 *
 * [countryCode] e o codigo ISO de duas letras do asset; o nome do pais e
 * resolvido na UI no idioma do app (`Locale`), para nao embarcar 3000 nomes
 * de pais traduzidos.
 */
data class City(
    val id: String,
    val name: String,
    val asciiName: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val population: Int,
)

/**
 * Catalogo em memoria lido de `assets/cities.txt` (gerado por
 * `tools/cities/build_cities.py`).
 *
 * O conteudo inteiro cabe em memoria (~3000 cidades) e e carregado uma vez;
 * a busca nao toca em disco.
 */
class CityCatalog(cities: List<City>) {

    private val cities: List<City> = cities.toList()
    private val index: Map<String, City> = this.cities.associateBy(City::id)

    /**
     * Texto normalizado de cada cidade (nome + asciiname). Pre-corrigido na
     * construcao para a busca nao normalizar 3000 nomes a cada tecla.
     */
    private val haystacks: List<Pair<String, City>> = this.cities.map { city ->
        normalize("${city.name} ${city.asciiName}") to city
    }

    fun all(): List<City> = cities

    fun byIdOrNull(id: String): City? = index[id]

    /** Id desconhecido (dado antigo ou corrompido) cai na primeira cidade. */
    fun byId(id: String): City = index[id] ?: first()

    fun first(): City = cities.firstOrNull() ?: error("catalogo de cidades vazio")

    /**
     * Busca por nome/asciiname sem diferenciar acento nem caixa.
     *
     * Todos os termos precisam aparecer, entao "sao paulo" acha "São Paulo".
     * O resultado segue a ordem do catalogo (populacao decrescente) e para em
     * [limit]; consulta vazia devolve as primeiras cidades.
     */
    fun search(query: String, limit: Int = MAX_RESULTS): List<City> {
        val terms = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (terms.isEmpty()) return cities.take(limit)
        val found = ArrayList<City>(minOf(limit, 64))
        for ((haystack, city) in haystacks) {
            if (terms.all { haystack.contains(it) }) {
                found += city
                if (found.size >= limit) break
            }
        }
        return found
    }

    /** Maior cidade do fuso, para o padrao quando nao ha cidade salva. */
    fun defaultForZone(zoneId: String): City? =
        cities.filter { it.zoneId == zoneId }.maxByOrNull { it.population }

    /**
     * Cidade mais proxima de uma coordenada (distancia haversine).
     *
     * Converte o fix do GPS em uma linha do catalogo, porque o app precisa de
     * nome e fuso IANA, nao apenas do ponto. Sempre devolve uma cidade: no
     * meio do oceano cai na mais proxima, e nao ha "sem resultado".
     */
    fun nearest(latitude: Double, longitude: Double): City {
        var best = cities.firstOrNull() ?: error("catalogo de cidades vazio")
        var bestDistance = Double.MAX_VALUE
        for (city in cities) {
            val distance = haversineKm(latitude, longitude, city.latitude, city.longitude)
            if (distance < bestDistance) {
                bestDistance = distance
                best = city
            }
        }
        return best
    }

    companion object {
        const val ASSET = "cities.txt"
        const val MAX_RESULTS = 60

        /** Raio medio da Terra em km (IUGG). */
        private const val EARTH_RADIUS_KM = 6371.0088

        /** Distancia em km entre dois pontos; o delta de longitude ja fecha 360°. */
        fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val sinLat = sin(dLat / 2)
            val sinLon = sin(dLon / 2)
            val a = sinLat * sinLat +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinLon * sinLon
            return 2 * EARTH_RADIUS_KM * asin(sqrt(a.coerceIn(0.0, 1.0)))
        }

        fun fromAsset(context: Context): CityCatalog {
            val cities = context.assets.open(ASSET).bufferedReader().useLines { lines ->
                lines.mapNotNull(::parse).toList()
            }
            check(cities.isNotEmpty()) { "$ASSET sem cidades; rode tools/cities/build_cities.py" }
            return CityCatalog(cities)
        }

        /** Entrada dos testes JVM, que nao tem `Context` para ler o asset. */
        fun fromLines(lines: List<String>): CityCatalog =
            CityCatalog(lines.mapNotNull(::parse))

        /** Linha do asset: `id|name|asciiname|countryCode|lat|lon|zone|population`. */
        fun parse(line: String): City? {
            val parts = line.split('|')
            if (parts.size < 7) return null
            val zone = parts[6].trim()
            if (zone.isEmpty()) return null
            return City(
                id = parts[0].trim(),
                name = parts[1].trim(),
                asciiName = parts[2].trim(),
                countryCode = parts[3].trim(),
                latitude = parts[4].trim().toDoubleOrNull() ?: return null,
                longitude = parts[5].trim().toDoubleOrNull() ?: return null,
                zoneId = zone,
                population = parts.getOrNull(7)?.trim()?.toIntOrNull() ?: 0,
            )
        }

        private val accents = Regex("\\p{Mn}+")
        private val spaces = Regex("\\s+")

        /** Minusculas, sem acento e com letras que o NFD nao decompoe. */
        fun normalize(text: String): String {
            val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
            var plain = accents.replace(decomposed, "").lowercase(Locale.ROOT)
            plain = plain
                .replace("ß", "ss")
                .replace("æ", "ae")
                .replace("ø", "o")
                .replace("ł", "l")
                .replace("đ", "d")
                .replace("ı", "i")
                .replace("þ", "th")
            return spaces.replace(plain, " ").trim()
        }
    }
}

/** Nome do pais para exibir na UI, no idioma do app. */
object CountryNames {
    fun display(countryCode: String, locale: Locale): String {
        if (countryCode.isEmpty()) return ""
        return runCatching {
            Locale.Builder().setRegion(countryCode).build().getDisplayCountry(locale)
        }.getOrDefault(countryCode)
    }
}
