package com.farukzahra.muslimplus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private data class LayerOption(val mode: String, val labelRes: Int)

private val layers = listOf(
    LayerOption("ar", R.string.quran_arabic),
    LayerOption("translit", R.string.quran_transliteration),
    LayerOption("pt-BR", R.string.language_portuguese),
    LayerOption("en", R.string.language_english),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDisplayScreen(onBack: () -> Unit) {
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val modes by settings.quranModes.collectAsState(initial = setOf("ar"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_quran_display)) },
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
                .padding(padding),
        ) {
            layers.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val current = settings.quranModes.first()
                                val next = if (option.mode in current) {
                                    current - option.mode
                                } else {
                                    current + option.mode
                                }
                                if (next.isNotEmpty()) settings.setQuranModes(next)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = option.mode in modes,
                        onCheckedChange = {
                            scope.launch {
                                val current = settings.quranModes.first()
                                val next = if (option.mode in current) {
                                    current - option.mode
                                } else {
                                    current + option.mode
                                }
                                if (next.isNotEmpty()) settings.setQuranModes(next)
                            }
                        },
                    )
                    Text(
                        text = stringResource(option.labelRes),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.quran_keep_one),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
