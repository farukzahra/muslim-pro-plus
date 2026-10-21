package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ui.components.openUrl

/** Endereco publicado da politica; o gate de release exige HTTP 200. */
const val PRIVACY_URL = "https://muslimplus.faruk.dev.br/privacy/"

/**
 * Resumo de privacidade em uma tela: sem conta, sem ads, sem analytics e a
 * localizacao fica no aparelho. O texto completo esta na pagina publicada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showFallback by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_privacy)) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.privacy_no_account),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Text(
                text = stringResource(R.string.privacy_no_ads),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Text(
                text = stringResource(R.string.privacy_location),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Text(
                text = stringResource(R.string.privacy_local_data),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Button(
                onClick = {
                    if (!openUrl(context, PRIVACY_URL)) showFallback = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.privacy_open_policy))
            }
        }
    }

    if (showFallback) {
        AlertDialog(
            onDismissRequest = { showFallback = false },
            title = { Text(stringResource(R.string.privacy_open_failed_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.privacy_open_failed_body))
                    Text(
                        text = PRIVACY_URL,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFallback = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }
}
