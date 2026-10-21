package com.farukzahra.muslimplus.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Coordenada do aparelho: ponto cru, sem nome de cidade nem fuso. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

/**
 * Localizacao pelo [LocationManager] nativo (GPS + rede), sem Play Services.
 *
 * O app so precisa de um ponto para achar a cidade mais proxima no catalogo,
 * entao tudo aqui e "one shot": nao existe assinatura de atualizacoes
 * continuas. Sem permissao ou sem fix dentro do timeout, [current] devolve
 * `null` e a UI cai no modo manual.
 */
class LocationRepository(
    context: Context,
    private val timeoutMillis: Long = TIMEOUT_MS,
) {
    private val context: Context = context.applicationContext

    /**
     * Ultimo fix conhecido se ele for recente ([FRESH_FIX_MILLIS]); caso
     * contrario espera um fix novo, no maximo [timeoutMillis].
     *
     * `@SuppressLint` porque o lint nao segue o early return de [hasPermission]
     * ate as chamadas do [LocationManager].
     */
    @SuppressLint("MissingPermission")
    suspend fun current(): Coordinates? {
        if (!hasPermission(context)) return null
        return try {
            val manager = context.getSystemService(LocationManager::class.java)
                ?: return null
            // Fix recente evita girar o GPS so para trocar de cidade.
            recentFix(manager)?.toCoordinates() ?: firstFix(manager)?.toCoordinates()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // Provedor pode morrer ou a permissao ser revogada no meio: a UI
            // cai no fallback manual em vez de derrubar a tela.
            null
        }
    }

    /** Fix valido mais recente entre os provedores, se ainda estiver fresco. */
    @SuppressLint("MissingPermission")
    private fun recentFix(manager: LocationManager): Location? =
        providers(manager)
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .filter { ageMillis(it) <= FRESH_FIX_MILLIS }
            .maxByOrNull { it.elapsedRealtimeNanos }

    /**
     * Dispara os provedores disponiveis em paralelo e fica com o primeiro fix.
     * O GPS pode demorar ou nao pegar (ambiente fechado), por isso a rede
     * corre junto em vez de esperar o GPS esgotar o timeout.
     */
    private suspend fun firstFix(manager: LocationManager): Location? = coroutineScope {
        val providers = providers(manager)
        if (providers.isEmpty()) return@coroutineScope null

        val fixes = Channel<Location>(Channel.CONFLATED)
        val jobs = providers.map { provider ->
            launch { requestFix(manager, provider)?.let { fixes.trySend(it) } }
        }
        val fix = withTimeoutOrNull(timeoutMillis) { fixes.receive() }
        jobs.forEach { it.cancel() }
        fixes.close()
        fix
    }

    /**
     * Um unico fix do provedor. API 30+ usa [LocationManager.getCurrentLocation],
     * anterior usa `requestSingleUpdate` (depreciado, mas e o equivalente ate 29).
     * A cancelacao do coroutine remove o pedido, sem deixar listener orfao.
     */
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private suspend fun requestFix(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                manager.getCurrentLocation(
                    provider,
                    signal,
                    ContextCompat.getMainExecutor(context),
                ) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                continuation.invokeOnCancellation { signal.cancel() }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }

                    override fun onProviderDisabled(provider: String) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(null)
                    }

                    override fun onProviderEnabled(provider: String) = Unit

                    @Deprecated("Deprecated in the Android API")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) =
                        Unit
                }
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            }
        }

    /** Provedores ligados no aparelho; GPS primeiro por ser mais preciso. */
    private fun providers(manager: LocationManager): List<String> =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }

    private fun Location.toCoordinates(): Coordinates =
        Coordinates(latitude = latitude, longitude = longitude)

    private fun ageMillis(location: Location): Long =
        (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000

    companion object {
        /** Teto de espera por um fix novo. */
        const val TIMEOUT_MS = 15_000L

        /** Fix de ate 5 minutos serve: evita girar o GPS para trocar de cidade. */
        const val FRESH_FIX_MILLIS = 5 * 60 * 1000L

        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        /** Basta uma das duas: coarse ja resolve a cidade mais proxima. */
        fun hasPermission(context: Context): Boolean =
            PERMISSIONS.any { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }
    }
}
