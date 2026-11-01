package com.farukzahra.muslimplus

import android.app.Application
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.notifications.Notifications
import com.farukzahra.muslimplus.prayer.PrayerLogRepository
import com.farukzahra.muslimplus.quran.QuranRepository
import com.farukzahra.muslimplus.quran.ReaderStateRepository
import com.farukzahra.muslimplus.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ServiceLocator {
    lateinit var cities: CityCatalog
    lateinit var settings: SettingsRepository
    lateinit var quran: QuranRepository
    lateinit var readerState: ReaderStateRepository
    lateinit var prayerLog: PrayerLogRepository
}

class MuslimPlusApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Catalogo em memoria (~3000 linhas) e carregado uma vez; SettingsRepository
        // usa ele para resolver a cidade padrao pelo fuso do aparelho.
        ServiceLocator.cities = CityCatalog.fromAsset(this)
        ServiceLocator.settings = SettingsRepository(this, ServiceLocator.cities)
        ServiceLocator.quran = QuranRepository(this)
        ServiceLocator.readerState = ReaderStateRepository(this)
        ServiceLocator.prayerLog = PrayerLogRepository(this)
        appScope.launch { ServiceLocator.settings.migrateAdhan() }
        Notifications.ensureChannel(this)
    }
}
