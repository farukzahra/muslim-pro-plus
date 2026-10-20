package com.farukzahra.muslimplus.location

import java.time.ZoneId

data class GeoLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val zone: ZoneId,
)

object DefaultLocations {
    val SaoPaulo = GeoLocation(
        name = "São Paulo",
        latitude = -23.5505,
        longitude = -46.6333,
        zone = ZoneId.of("America/Sao_Paulo"),
    )
}
