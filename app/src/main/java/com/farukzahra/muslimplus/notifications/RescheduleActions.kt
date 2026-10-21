package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import kotlinx.coroutines.runBlocking

/**
 * Acoes de sistema que invalidam o proximo alarme agendado. Os valores sao
 * literais (e nao `Intent.ACTION_*`) para a regra rodar em teste JVM sem
 * android.jar; `RescheduleIntentsTest` confere as constantes e o manifesto.
 */
object RescheduleActions {
    const val BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    const val TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED"
    const val TIME_SET = "android.intent.action.TIME_SET"
    const val MY_PACKAGE_REPLACED = "android.intent.action.MY_PACKAGE_REPLACED"

    private val HANDLED = setOf(
        BOOT_COMPLETED,
        TIMEZONE_CHANGED,
        TIME_SET,
        MY_PACKAGE_REPLACED,
    )

    fun handles(action: String?): Boolean = action != null && action in HANDLED
}

/**
 * Reagenda fora da main thread sem deixar o receiver ser encerrado no meio.
 */
internal fun BroadcastReceiver.rescheduleInBackground(context: Context) {
    val pending = goAsync()
    Thread {
        try {
            runBlocking { PrayerScheduler(context.applicationContext).rescheduleNow() }
        } finally {
            pending.finish()
        }
    }.start()
}
