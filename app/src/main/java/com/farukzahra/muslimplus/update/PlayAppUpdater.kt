package com.farukzahra.muslimplus.update

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

/**
 * Atualizacao flexivel do Play: baixa em segundo plano e reinicia quando o
 * usuario mandar. Fora da Play Store (debug instalado por adb) a consulta
 * simplesmente falha e o app segue na versao atual.
 */
class PlayAppUpdater(context: Context) : AppUpdater {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
    private var pendingUpdate: AppUpdateInfo? = null

    override suspend fun check(): UpdateSignals {
        val info = manager.appUpdateInfo.awaitOrNull() ?: return UpdateSignals()
        pendingUpdate = info
        return info.toSignals()
    }

    override fun observeInstallState(): Flow<UpdateSignals> = callbackFlow {
        val listener = InstallStateUpdatedListener { state -> trySend(state.toSignals()) }
        manager.registerListener(listener)
        awaitClose { manager.unregisterListener(listener) }
    }

    override suspend fun startFlexibleUpdate(activity: Activity): Boolean {
        val info = pendingUpdate ?: manager.appUpdateInfo.awaitOrNull() ?: return false
        pendingUpdate = info
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
        return try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            manager.startUpdateFlowForResult(info, activity, options, UPDATE_REQUEST_CODE)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun completeUpdate() {
        try {
            manager.completeUpdate().awaitOrNull()
        } catch (_: Exception) {
            // Sem o Play por perto, o app segue na versao atual.
        }
    }

    private fun AppUpdateInfo.toSignals(): UpdateSignals {
        val availability = when {
            installStatus() == InstallStatus.DOWNLOADED -> UpdateAvailability.Downloaded
            installStatus() == InstallStatus.DOWNLOADING -> UpdateAvailability.Downloading
            updateAvailability() == PlayUpdateAvailability.UPDATE_AVAILABLE -> UpdateAvailability.Available
            updateAvailability() == PlayUpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                UpdateAvailability.Downloading

            updateAvailability() == PlayUpdateAvailability.UPDATE_NOT_AVAILABLE -> UpdateAvailability.None
            else -> UpdateAvailability.Unknown
        }
        return UpdateSignals(availability, bytesDownloaded(), totalBytesToDownload())
    }

    private fun InstallState.toSignals(): UpdateSignals {
        val availability = when (installStatus()) {
            InstallStatus.DOWNLOADED -> UpdateAvailability.Downloaded
            InstallStatus.DOWNLOADING -> UpdateAvailability.Downloading
            InstallStatus.INSTALLED -> UpdateAvailability.None
            else -> pendingUpdate?.toSignals()?.availability ?: UpdateAvailability.Unknown
        }
        return UpdateSignals(availability, bytesDownloaded(), totalBytesToDownload())
    }

    private companion object {
        const val UPDATE_REQUEST_CODE = 4201
    }
}

private suspend fun <T> Task<T>.awaitOrNull(): T? = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { continuation.resume(null) }
}
