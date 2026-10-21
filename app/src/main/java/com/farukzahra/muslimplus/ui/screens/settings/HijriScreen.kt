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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.settings.HijriOffset
import kotlinx.coroutines.launch

private data class HijriOption(val days: Int, val labelRes: Int)

private val options = listOf(
    HijriOption(-1, R.string.hijri_minus_one),
    HijriOption(HijriOffset.DEFAULT, R.string.hijri_default),
    HijriOption(1, R.string.hijri_plus_one),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriScreen(onBack: () -> Unit) {
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    val current by settings.hijriOffset.collectAsState(initial = HijriOffset.DEFAULT)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_hijri)) },
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
            options.forEach { option ->
                ListItem(
                    headlineContent = { Text(stringResource(option.labelRes)) },
                    leadingContent = {
                        RadioButton(
                            selected = current == option.days,
                            onClick = {
                                scope.launch { settings.setHijriOffset(option.days) }
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = current == option.days,
                            onClick = {
                                scope.launch { settings.setHijriOffset(option.days) }
                            },
                        ),
                )
            }
            Text(
                text = stringResource(R.string.hijri_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
