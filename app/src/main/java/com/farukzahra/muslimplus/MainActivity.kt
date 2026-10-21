package com.farukzahra.muslimplus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.settings.SettingsRepository
import com.farukzahra.muslimplus.ui.MuslimPlusApp
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme

class MainActivity : AppCompatActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) PrayerScheduler(this).reschedule()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        PrayerScheduler(this).reschedule()

        setContent {
            val mode by ServiceLocator.settings.themeMode.collectAsState(initial = "dark")
            val dark = when (mode) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true
            }
            MuslimPlusTheme(darkTheme = dark) {
                MuslimPlusApp()
            }
        }
    }
}
