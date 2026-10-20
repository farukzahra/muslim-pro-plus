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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.settings.LanguageRepository

private data class LanguageOption(val tag: String?, val labelRes: Int)

private val options = listOf(
    LanguageOption(null, R.string.language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("pt-BR", R.string.language_portuguese),
    LanguageOption("ar", R.string.language_arabic),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val current = LanguageRepository.currentTag()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_title)) },
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
                            selected = current == option.tag,
                            onClick = { LanguageRepository.set(option.tag) },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = current == option.tag,
                            onClick = { LanguageRepository.set(option.tag) },
                        ),
                )
            }
        }
    }
}
