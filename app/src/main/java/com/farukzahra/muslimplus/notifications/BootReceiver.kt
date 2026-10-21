package com.farukzahra.muslimplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!RescheduleActions.handles(intent.action)) return
        rescheduleInBackground(context)
    }
}
