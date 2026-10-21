package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import kotlinx.coroutines.launch

private data class ThemeOption(val mode: String, val labelRes: Int)

private val options = listOf(
    ThemeOption("dark", R.string.theme_dark),
    ThemeOption("light", R.string.theme_light),
    ThemeOption("system", R.string.theme_system),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(onBack: () -> Unit) {
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val current by settings.themeMode.collectAsState(initial = "dark")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_theme)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            options.forEach { option ->
                ListItem(
                    headlineContent = { Text(stringResource(option.labelRes)) },
                    leadingContent = {
                        RadioButton(
                            selected = current == option.mode,
                            onClick = {
                                scope.launch { settings.setThemeMode(option.mode) }
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = current == option.mode,
                            onClick = {
                                scope.launch { settings.setThemeMode(option.mode) }
                            },
                        ),
                )
            }
        }
    }
}
