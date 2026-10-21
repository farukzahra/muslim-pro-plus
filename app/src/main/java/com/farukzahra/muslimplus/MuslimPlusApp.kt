package com.farukzahra.muslimplus

import android.app.Application
import com.farukzahra.muslimplus.notifications.Notifications
import com.farukzahra.muslimplus.quran.QuranRepository
import com.farukzahra.muslimplus.settings.SettingsRepository

object ServiceLocator {
    lateinit var settings: SettingsRepository
    lateinit var quran: QuranRepository
}

class MuslimPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.settings = SettingsRepository(this)
        ServiceLocator.quran = QuranRepository(this)
        Notifications.ensureChannel(this)
    }
}
