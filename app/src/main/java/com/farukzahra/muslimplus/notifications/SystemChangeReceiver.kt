package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fuso, hora do aparelho e atualizacao do app mudam o horario da proxima
 * oracao; sem reagendar, o alarme tocaria na hora antiga.
 */
class SystemChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!RescheduleActions.handles(intent.action)) return
        rescheduleInBackground(context)
    }
}
