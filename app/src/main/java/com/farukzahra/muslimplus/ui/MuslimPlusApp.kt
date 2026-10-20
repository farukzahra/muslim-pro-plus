package com.farukzahra.muslimplus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farukzahra.muslimplus.ui.nav.Tab
import com.farukzahra.muslimplus.ui.screens.PlaceholderScreen

@Composable
fun MuslimPlusApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Tab.TODAY.route

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        icon = { Icon(iconFor(tab), contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
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
                    PlaceholderScreen(title = stringResource(tab.labelRes))
                }
            }
        }
    }
}

private fun iconFor(tab: Tab): ImageVector = when (tab) {
    Tab.TODAY -> Icons.Outlined.Home
    Tab.PRAYERS -> Icons.AutoMirrored.Outlined.List
    Tab.QURAN -> Icons.Outlined.Menu
    Tab.QIBLA -> Icons.Outlined.Place
    Tab.MORE -> Icons.Outlined.MoreVert
}
