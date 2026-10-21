package com.farukzahra.muslimplus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ui.nav.Tab
import com.farukzahra.muslimplus.ui.screens.PlaceholderScreen
import com.farukzahra.muslimplus.ui.screens.more.MoreScreen
import com.farukzahra.muslimplus.ui.screens.prayers.PrayersScreen
import com.farukzahra.muslimplus.ui.screens.qibla.QiblaScreen
import com.farukzahra.muslimplus.ui.screens.quran.QuranListScreen
import com.farukzahra.muslimplus.ui.screens.quran.QuranReaderScreen
import com.farukzahra.muslimplus.ui.screens.settings.HijriScreen
import com.farukzahra.muslimplus.ui.screens.settings.LanguageScreen
import com.farukzahra.muslimplus.ui.screens.settings.QuranDisplayScreen
import com.farukzahra.muslimplus.ui.screens.settings.ThemeScreen
import com.farukzahra.muslimplus.ui.screens.today.TodayScreen

const val LANGUAGE_ROUTE = "settings/language"
const val QURAN_DISPLAY_ROUTE = "settings/quran-display"
const val THEME_ROUTE = "settings/theme"
const val HIJRI_ROUTE = "settings/hijri"

/** Leitor do Alcorao; `ayah` e opcional e 0 significa "continuar da ultima leitura". */
const val QURAN_READER_ROUTE = "quran/{surah}?ayah={ayah}"
private const val NO_AYAH = 0

/** Rota do leitor. Sem [ayah] a tela abre na ultima leitura daquela sura. */
fun quranReaderRoute(surah: Int, ayah: Int = NO_AYAH): String = "quran/$surah?ayah=$ayah"

@Composable
fun MuslimPlusApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Tab.TODAY.route
    val showTabs = Tab.entries.any { it.route == currentRoute }

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
                                    painter = painterResource(iconRes(tab)),
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
        NavHost(
            navController = navController,
            startDestination = Tab.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            Tab.entries.forEach { tab ->
                composable(tab.route) {
                    when (tab) {
                        Tab.TODAY -> TodayScreen()
                        Tab.PRAYERS -> PrayersScreen()
                        Tab.QURAN -> QuranListScreen(
                            onOpenSurah = { surah -> navController.navigate(quranReaderRoute(surah)) },
                            onOpenVerse = { surah, ayah ->
                                navController.navigate(quranReaderRoute(surah, ayah))
                            },
                        )
                        Tab.QIBLA -> QiblaScreen()
                        Tab.MORE -> MoreScreen(
                            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) },
                            onQuranDisplayClick = { navController.navigate(QURAN_DISPLAY_ROUTE) },
                            onThemeClick = { navController.navigate(THEME_ROUTE) },
                            onHijriClick = { navController.navigate(HIJRI_ROUTE) },
                        )
                    }
                }
            }
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
                QuranReaderScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun iconRes(tab: Tab): Int = when (tab) {
    Tab.TODAY -> R.drawable.tab_today
    Tab.PRAYERS -> R.drawable.tab_prayers
    Tab.QURAN -> R.drawable.tab_quran
    Tab.QIBLA -> R.drawable.tab_qibla
    Tab.MORE -> R.drawable.tab_more
}
