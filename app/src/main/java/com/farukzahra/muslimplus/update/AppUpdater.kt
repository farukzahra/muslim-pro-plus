package com.farukzahra.muslimplus.update

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Fronteira com o Play; testes e builds sem Play usam um fake. */
interface AppUpdater {
    suspend fun check(): UpdateSignals

    fun observeInstallState(): Flow<UpdateSignals>

    suspend fun startFlexibleUpdate(activity: Activity): Boolean

    suspend fun completeUpdate()
}

object AppUpdaterProvider {
    var factory: (Context) -> AppUpdater = { context -> PlayAppUpdater(context) }

    fun create(context: Context): AppUpdater = factory(context.applicationContext)
}

/** Persistencia do "agora nao" do dialogo de atualizacao. */
interface UpdatePromptStore {
    fun observePromptDismissed(): Flow<Boolean>

    suspend fun setPromptDismissed(value: Boolean)
}
