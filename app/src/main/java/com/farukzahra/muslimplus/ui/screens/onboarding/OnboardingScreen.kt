package com.farukzahra.muslimplus.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.LocationRepository
import com.farukzahra.muslimplus.notifications.PrayerScheduler
import com.farukzahra.muslimplus.prayer.PrayerMethod
import com.farukzahra.muslimplus.settings.LanguageRepository
import com.farukzahra.muslimplus.ui.components.ChoiceDialog
import com.farukzahra.muslimplus.ui.components.CityPickerDialog
import com.farukzahra.muslimplus.ui.screens.settings.LanguageScreen
import com.farukzahra.muslimplus.ui.screens.settings.languageLabelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Onboarding de primeira execucao: uma unica tela rolavel com quatro blocos
 * (idioma, localizacao, metodo, notificacoes). Cada escolha e persistida na
 * hora; "Comecar" e "Pular" apenas gravam a flag e abrem o app.
 *
 * Tudo o que e configuravel depois tambem esta nas telas normais, entao pular
 * nao perde nenhuma decisao: os padroes (idioma do aparelho, cidade do fuso)
 * seguem valendo.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = ServiceLocator.settings

    var showLanguage by remember { mutableStateOf(false) }
    var showCities by remember { mutableStateOf(false) }
    var showMethods by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    var locationFailed by remember { mutableStateOf(false) }

    val cityId by settings.cityId.collectAsState(initial = null)
    val methodName by settings.methodName.collectAsState(initial = PrayerMethod.MWL.name)
    val notificationsEnabled by settings.notificationsEnabled.collectAsState(initial = true)

    val city = cityId?.let { ServiceLocator.cities.byIdOrNull(it) }
    val method = runCatching { PrayerMethod.valueOf(methodName) }.getOrDefault(PrayerMethod.MWL)

    fun locate() {
        locating = true
        locationFailed = false
        scope.launch {
            val coordinates = withContext(Dispatchers.IO) {
                LocationRepository(context).current()
            }
            val found = coordinates?.let {
                ServiceLocator.cities.nearest(it.latitude, it.longitude)
            }
            if (found == null) {
                locationFailed = true
            } else {
                settings.setCity(found.id)
            }
            locating = false
        }
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) locate() else locationFailed = true
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun finish() {
        if (notificationsEnabled) requestNotificationsIfNeeded()
        scope.launch {
            settings.setOnboardingDone()
            PrayerScheduler(context).reschedule()
            onDone()
        }
    }

    // A tela de idioma e reaproveitada; sem nav graph, um estado local troca o
    // conteudo. Trocar o idioma recria a activity e volta para o onboarding.
    if (showLanguage) {
        LanguageScreen(onBack = { showLanguage = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Section(title = stringResource(R.string.language_title)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(languageLabelRes(LanguageRepository.currentTag())),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                TextButton(onClick = { showLanguage = true }) {
                    Text(stringResource(R.string.onboarding_change))
                }
            }
            Text(
                text = stringResource(R.string.onboarding_language_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Section(title = stringResource(R.string.onboarding_location_title)) {
            Text(
                text = stringResource(R.string.onboarding_location_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (LocationRepository.hasPermission(context)) {
                            locate()
                        } else {
                            locationPermission.launch(LocationRepository.PERMISSIONS)
                        }
                    },
                    enabled = !locating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_use_location))
                }
                OutlinedButton(
                    onClick = { showCities = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_choose_city))
                }
            }
            val status = when {
                locating -> stringResource(R.string.onboarding_location_searching)
                locationFailed -> stringResource(R.string.onboarding_location_failed)
                city != null -> stringResource(R.string.onboarding_location_selected, city.name)
                else -> null
            }
            if (status != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (locationFailed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        Section(title = stringResource(R.string.prayers_method)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = method.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                TextButton(onClick = { showMethods = true }) {
                    Text(stringResource(R.string.onboarding_change))
                }
            }
        }

        Section(title = stringResource(R.string.more_notifications)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.onboarding_notifications_hint),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) requestNotificationsIfNeeded()
                        scope.launch { settings.setNotificationsEnabled(enabled) }
                    },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { finish() }) {
                Text(stringResource(R.string.onboarding_skip))
            }
            Button(onClick = { finish() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.onboarding_start))
            }
        }
    }

    if (showCities) {
        CityPickerDialog(
            onSearch = { query -> ServiceLocator.cities.search(query) },
            onPick = { id ->
                locationFailed = false
                scope.launch { settings.setCity(id) }
            },
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
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(4.dp))
    Column(modifier = Modifier.fillMaxWidth(), content = content)
}
