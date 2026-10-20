package com.farukzahra.muslimplus.location

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
)

/**
 * Subset inicial de cidades. O catalogo completo (GeoNames cities5000) entra
 * no milestone de localizacao; aqui ficam as capitais mais usadas.
 */
object CityCatalog {
    val cities: List<City> = listOf(
        City("sao-paulo", "São Paulo", "Brasil", -23.5505, -46.6333, "America/Sao_Paulo"),
        City("rio", "Rio de Janeiro", "Brasil", -22.9068, -43.1729, "America/Sao_Paulo"),
        City("brasilia", "Brasília", "Brasil", -15.7939, -47.8828, "America/Sao_Paulo"),
        City("salvador", "Salvador", "Brasil", -12.9777, -38.5016, "America/Bahia"),
        City("makkah", "Makkah", "Arábia Saudita", 21.4225, 39.8262, "Asia/Riyadh"),
        City("madinah", "Madinah", "Arábia Saudita", 24.4672, 39.6111, "Asia/Riyadh"),
        City("cairo", "Cairo", "Egito", 30.0444, 31.2357, "Africa/Cairo"),
        City("istanbul", "Istambul", "Turquia", 41.0082, 28.9784, "Europe/Istanbul"),
        City("dubai", "Dubai", "Emirados Árabes", 25.2048, 55.2708, "Asia/Dubai"),
        City("london", "Londres", "Reino Unido", 51.5074, -0.1278, "Europe/London"),
        City("lisbon", "Lisboa", "Portugal", 38.7223, -9.1393, "Europe/Lisbon"),
        City("paris", "Paris", "França", 48.8566, 2.3522, "Europe/Paris"),
        City("new-york", "Nova York", "EUA", 40.7128, -74.0060, "America/New_York"),
        City("toronto", "Toronto", "Canadá", 43.6532, -79.3832, "America/Toronto"),
        City("jakarta", "Jacarta", "Indonésia", -6.2088, 106.8456, "Asia/Jakarta"),
        City("kuala-lumpur", "Kuala Lumpur", "Malásia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        City("lagos", "Lagos", "Nigéria", 6.5244, 3.3792, "Africa/Lagos"),
        City("cape-town", "Cidade do Cabo", "África do Sul", -33.9249, 18.4241, "Africa/Johannesburg"),
    )

    fun byId(id: String): City = cities.firstOrNull { it.id == id } ?: cities.first()

    fun search(query: String): List<City> {
        val term = query.trim()
        if (term.isEmpty()) return cities
        return cities.filter {
            it.name.contains(term, ignoreCase = true) ||
                it.country.contains(term, ignoreCase = true)
        }
    }
}
