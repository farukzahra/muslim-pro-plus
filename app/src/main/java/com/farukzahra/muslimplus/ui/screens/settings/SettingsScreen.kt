package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.CountryNames
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import com.farukzahra.muslimplus.ui.components.ChoiceDialog
import com.farukzahra.muslimplus.ui.components.CityPickerDialog
import com.farukzahra.muslimplus.ui.components.OffsetsDialog
import kotlinx.coroutines.launch

/**
 * Configuracoes reunidas em um lugar: cidade, metodo, ajustes por oracao,
 * data hijri e notificacoes. As mesmas preferencias do DataStore usadas pela
 * aba Oracoes e pelo onboarding, entao mudar aqui reflete em todo o app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onHijriClick: () -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val scope = rememberCoroutineScope()
    val settings = ServiceLocator.settings
    val cities = ServiceLocator.cities

    var showCities by remember { mutableStateOf(false) }
    var showMethods by remember { mutableStateOf(false) }
    var showOffsets by remember { mutableStateOf(false) }

    // Vazio ate o primeiro valor do DataStore chegar; nao ha tela de loading
    // porque o catalogo ja esta em memoria e o fluxo emite na hora.
    val cityId by settings.cityId.collectAsState(initial = "")
    val methodName by settings.methodName.collectAsState(initial = PrayerMethod.MWL.name)
    val offsets by settings.prayerOffsets.collectAsState(initial = PrayerOffsets.defaults)
    val notificationsEnabled by settings.notificationsEnabled.collectAsState(initial = true)

    val city = cities.byIdOrNull(cityId)
    val cityLabel = if (city == null) {
        ""
    } else {
        stringResource(
            R.string.city_with_country,
            city.name,
            CountryNames.display(city.countryCode, locale),
        )
    }
    val method = runCatching { PrayerMethod.valueOf(methodName) }
        .getOrDefault(PrayerMethod.MWL)

    val setNotifications: (Boolean) -> Unit = { enabled ->
        scope.launch {
            settings.setNotificationsEnabled(enabled)
            PrayerScheduler(context).reschedule()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_settings)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsRow(
                title = stringResource(R.string.prayers_city),
                value = cityLabel,
                onClick = { showCities = true },
            )
            HorizontalDivider()
            SettingsRow(
                title = stringResource(R.string.prayers_method),
                value = method.name,
                onClick = { showMethods = true },
            )
            HorizontalDivider()
            SettingsRow(
                title = stringResource(R.string.prayers_offsets_title),
                value = stringResource(R.string.prayers_offsets_hint),
                onClick = { showOffsets = true },
            )
            HorizontalDivider()
            SettingsRow(
                title = stringResource(R.string.more_hijri),
                value = stringResource(R.string.settings_hijri_hint),
                onClick = onHijriClick,
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.more_notifications)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_notifications_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = setNotifications,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setNotifications(!notificationsEnabled) },
            )
        }
    }

    if (showCities) {
        CityPickerDialog(
            onSearch = { query -> cities.search(query) },
            onPick = { id -> scope.launch { settings.setCity(id) } },
            onDismiss = { showCities = false },
        )
    }
    if (showMethods) {
        ChoiceDialog(
            title = stringResource(R.string.prayers_method),
            options = PrayerMethod.entries.map { it.name to it.name },
            onPick = { name -> scope.launch { settings.setMethod(name) } },
            onDismiss = { showMethods = false },
        )
    }
    if (showOffsets) {
        OffsetsDialog(
            offsets = offsets,
            onAdjust = { prayer, delta -> scope.launch { settings.nudgePrayerOffset(prayer, delta) } },
            onDismiss = { showOffsets = false },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
