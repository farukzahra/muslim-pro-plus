package com.farukzahra.muslimplus

import android.app.Application
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.notifications.Notifications
import com.farukzahra.muslimplus.prayer.PrayerLogRepository
import com.farukzahra.muslimplus.quran.QuranRepository
import com.farukzahra.muslimplus.quran.ReaderStateRepository
import com.farukzahra.muslimplus.settings.SettingsRepository

object ServiceLocator {
    lateinit var cities: CityCatalog
    lateinit var settings: SettingsRepository
    lateinit var quran: QuranRepository
    lateinit var readerState: ReaderStateRepository
    lateinit var prayerLog: PrayerLogRepository
}

class MuslimPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Catalogo em memoria (~3000 linhas) e carregado uma vez; SettingsRepository
        // usa ele para resolver a cidade padrao pelo fuso do aparelho.
        ServiceLocator.cities = CityCatalog.fromAsset(this)
        ServiceLocator.settings = SettingsRepository(this, ServiceLocator.cities)
        ServiceLocator.quran = QuranRepository(this)
        ServiceLocator.readerState = ReaderStateRepository(this)
        ServiceLocator.prayerLog = PrayerLogRepository(this)
        Notifications.ensureChannel(this)
    }
}
