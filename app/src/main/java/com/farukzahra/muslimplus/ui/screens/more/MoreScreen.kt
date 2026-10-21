package com.farukzahra.muslimplus.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    onLanguageClick: () -> Unit,
    onQuranDisplayClick: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = ServiceLocator.settings
    val notificationsEnabled by settings.notificationsEnabled.collectAsState(initial = true)

    Column(modifier = Modifier.fillMaxSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_language)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLanguageClick)
                .padding(horizontal = 4.dp),
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_quran_display)) },
            supportingContent = { Text(stringResource(R.string.more_quran_display_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onQuranDisplayClick)
                .padding(horizontal = 4.dp),
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_notifications)) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            settings.setNotificationsEnabled(enabled)
                            PrayerScheduler(context).reschedule()
                        }
                    },
                )
            },
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_about)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.about_version, "0.1.0"),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
    }
}
