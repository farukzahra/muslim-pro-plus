package com.farukzahra.muslimplus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.adhan.AdhanService
import com.farukzahra.muslimplus.ui.nav.Tab
import com.farukzahra.muslimplus.ui.screens.PlaceholderScreen
import com.farukzahra.muslimplus.ui.screens.more.MoreScreen
import com.farukzahra.muslimplus.ui.screens.prayers.PrayersScreen
import com.farukzahra.muslimplus.ui.screens.qibla.QiblaScreen
import com.farukzahra.muslimplus.ui.screens.quran.QuranListScreen
import com.farukzahra.muslimplus.ui.screens.quran.QuranReaderScreen
import com.farukzahra.muslimplus.ui.screens.settings.AboutScreen
import com.farukzahra.muslimplus.ui.screens.settings.HijriScreen
import com.farukzahra.muslimplus.ui.screens.settings.LanguageScreen
import com.farukzahra.muslimplus.ui.screens.settings.LicensesScreen
import com.farukzahra.muslimplus.ui.screens.settings.PrivacyScreen
import com.farukzahra.muslimplus.ui.screens.settings.QuranDisplayScreen
import com.farukzahra.muslimplus.ui.screens.settings.SettingsScreen
import com.farukzahra.muslimplus.ui.screens.settings.ThemeScreen
import com.farukzahra.muslimplus.ui.screens.today.TodayScreen
import com.farukzahra.muslimplus.update.UpdateCardState

const val AUDIO_STORAGE_ROUTE = "settings/audio-storage"
const val ADHAN_ROUTE = "settings/adhan"
const val SETTINGS_ROUTE = "settings"
const val LANGUAGE_ROUTE = "settings/language"
const val QURAN_DISPLAY_ROUTE = "settings/quran-display"
const val THEME_ROUTE = "settings/theme"
const val HIJRI_ROUTE = "settings/hijri"
const val LICENSES_ROUTE = "settings/licenses"
const val PRIVACY_ROUTE = "settings/privacy"
const val ABOUT_ROUTE = "settings/about"

/** Leitor do Alcorao; `ayah` e opcional e 0 significa "continuar da ultima leitura". */
const val QURAN_READER_ROUTE = "quran/{surah}?ayah={ayah}"
private const val NO_AYAH = 0

/** Rota do leitor. Sem [ayah] a tela abre na ultima leitura daquela sura. */
fun quranReaderRoute(surah: Int, ayah: Int = NO_AYAH): String = "quran/$surah?ayah=$ayah"

@Composable
fun MuslimPlusApp(
    updateCard: UpdateCardState = UpdateCardState.Unknown,
    updatePercent: Int? = null,
    onUpdateClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Tab.TODAY.route
    val showTabs = Tab.entries.any { it.route == currentRoute }
    val context = LocalContext.current
    val adhanPlaying by AdhanService.playing.collectAsState()

    Scaffold(
        bottomBar = {
            if (showTabs) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = iconFor(tab),
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (adhanPlaying) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adhan_playing_banner"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.adhan_playing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { AdhanService.stop(context) },
                            modifier = Modifier.testTag("adhan_stop_button"),
                        ) {
                            Text(
                                text = stringResource(R.string.adhan_stop),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
            NavHost(
                navController = navController,
                startDestination = Tab.TODAY.route,
                modifier = Modifier.weight(1f),
            ) {
            Tab.entries.forEach { tab ->
                composable(tab.route) {
                    when (tab) {
                        Tab.TODAY -> TodayScreen(
                    onOpenVerse = { surah, ayah ->
                        navController.navigate(quranReaderRoute(surah, ayah))
                    },
                )
                        Tab.PRAYERS -> PrayersScreen()
                        Tab.QURAN -> QuranListScreen(
                            onOpenSurah = { surah -> navController.navigate(quranReaderRoute(surah)) },
                            onOpenVerse = { surah, ayah ->
                                navController.navigate(quranReaderRoute(surah, ayah))
                            },
                        )
                        Tab.QIBLA -> QiblaScreen()
                        Tab.MORE -> MoreScreen(
                            onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
                            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) },
                            onQuranDisplayClick = { navController.navigate(QURAN_DISPLAY_ROUTE) },
                            onThemeClick = { navController.navigate(THEME_ROUTE) },
                            onLicensesClick = { navController.navigate(LICENSES_ROUTE) },
                            onAboutClick = { navController.navigate(ABOUT_ROUTE) },
                            updateCard = updateCard,
                            updatePercent = updatePercent,
                            onUpdateClick = onUpdateClick,
                            onRestartClick = onRestartClick,
                            onCheckUpdate = onCheckUpdate,
                        )
                    }
                }
            }
            composable(SETTINGS_ROUTE) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onHijriClick = { navController.navigate(HIJRI_ROUTE) },
                    onAudioStorageClick = { navController.navigate(AUDIO_STORAGE_ROUTE) },
                    onAdhanClick = { navController.navigate(ADHAN_ROUTE) },
                )
            }
            composable(ADHAN_ROUTE) {
                com.farukzahra.muslimplus.ui.screens.settings.AdhanScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AUDIO_STORAGE_ROUTE) { com.farukzahra.muslimplus.ui.screens.settings.AudioStorageScreen(onBack = { navController.popBackStack() }) }
            composable(LANGUAGE_ROUTE) {
                LanguageScreen(onBack = { navController.popBackStack() })
            }
            composable(QURAN_DISPLAY_ROUTE) {
                QuranDisplayScreen(onBack = { navController.popBackStack() })
            }
            composable(THEME_ROUTE) {
                ThemeScreen(onBack = { navController.popBackStack() })
            }
            composable(HIJRI_ROUTE) {
                HijriScreen(onBack = { navController.popBackStack() })
            }
            composable(LICENSES_ROUTE) {
                LicensesScreen(onBack = { navController.popBackStack() })
            }
            composable(PRIVACY_ROUTE) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }
            composable(ABOUT_ROUTE) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onPrivacyClick = { navController.navigate(PRIVACY_ROUTE) },
                )
            }
            composable(
                route = QURAN_READER_ROUTE,
                arguments = listOf(
                    navArgument("surah") { type = NavType.IntType },
                    navArgument("ayah") {
                        type = NavType.IntType
                        defaultValue = NO_AYAH
                    },
                ),
            ) {
                QuranReaderScreen(onBack = { navController.popBackStack() }, onAudioSettings = { navController.navigate(AUDIO_STORAGE_ROUTE) })
            }
        }
    }
}
}

private fun iconFor(tab: Tab): ImageVector = when (tab) {
    Tab.TODAY -> Icons.Outlined.Home
    Tab.PRAYERS -> Icons.Outlined.Mosque
    Tab.QURAN -> Icons.AutoMirrored.Outlined.MenuBook
    Tab.QIBLA -> Icons.Outlined.Explore
    Tab.MORE -> Icons.Outlined.MoreHoriz
}

