package com.farukzahra.muslimplus

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.settings.Onboarding
import com.farukzahra.muslimplus.settings.StartScreen
import com.farukzahra.muslimplus.ui.MuslimPlusApp
import com.farukzahra.muslimplus.ui.screens.onboarding.OnboardingScreen
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme
import com.farukzahra.muslimplus.ui.update.UpdateDialogs
import com.farukzahra.muslimplus.ui.update.UpdateViewModel
import com.farukzahra.muslimplus.ui.update.UpdateViewModelFactory
import com.farukzahra.muslimplus.update.AppUpdaterProvider
import kotlinx.coroutines.flow.first

class MainActivity : AppCompatActivity() {
    private val updateViewModel: UpdateViewModel by viewModels {
        UpdateViewModelFactory(
            AppUpdaterProvider.create(applicationContext),
            ServiceLocator.settings,
        )
    }

    override fun onResume() {
        super.onResume()
        updateViewModel.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Agenda o proximo alarme com a cidade salva; o onboarding reagenda de
        // novo quando a cidade/metodo mudam. A permissao de notificacao agora e
        // pedida no onboarding, junto do switch que a explica.
        PrayerScheduler(this).reschedule()

        setContent {
            val mode by ServiceLocator.settings.themeMode.collectAsState(initial = "dark")
            val dark = when (mode) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true
            }
            // null = lendo a flag do DataStore; evita piscar o onboarding para
            // quem ja concluiu.
            var screen by remember { mutableStateOf<StartScreen?>(null) }
            LaunchedEffect(Unit) {
                screen = Onboarding.startScreen(ServiceLocator.settings.onboardingDone.first())
            }
            val update by updateViewModel.uiState.collectAsState()

            MuslimPlusTheme(darkTheme = dark) {
                when (screen) {
                    null -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )
                    StartScreen.ONBOARDING -> OnboardingScreen(
                        onDone = { screen = StartScreen.HOME },
                    )
                    StartScreen.HOME -> MuslimPlusApp(
                        updateCard = update.card,
                        updatePercent = update.downloadPercent,
                        onUpdateClick = { updateViewModel.onUpdateClick(this@MainActivity) },
                        onRestartClick = updateViewModel::onRestartClick,
                        onCheckUpdate = updateViewModel::checkNow,
                    )
                }
                if (screen == StartScreen.HOME) {
                    UpdateDialogs(
                        dialog = update.dialog,
                        onUpdate = { updateViewModel.onUpdateClick(this@MainActivity) },
                        onLater = updateViewModel::onPromptDismissed,
                        onRestart = updateViewModel::onRestartClick,
                        onPostpone = updateViewModel::onRestartPostponed,
                    )
                }
            }
        }
    }
}
