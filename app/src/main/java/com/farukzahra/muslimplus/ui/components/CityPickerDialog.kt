package com.farukzahra.muslimplus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.location.CountryNames

/**
 * Busca de cidade do catalogo, usada na tela Oracoes e no onboarding.
 *
 * A busca roda no catalogo em memoria (~3000 nomes pre-normalizados), entao e
 * sincrona e nao precisa de estado de carregamento.
 */
@Composable
fun CityPickerDialog(
    onSearch: (String) -> List<City>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val locale = LocalConfiguration.current.locales[0]
    val results = remember(query) { onSearch(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prayers_city)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.prayers_city_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (results.isEmpty()) {
                    Text(
                        text = stringResource(R.string.prayers_city_none),
                        modifier = Modifier.padding(vertical = 16.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .padding(top = 8.dp),
                    ) {
                        items(results, key = { it.id }) { city ->
                            Text(
                                text = stringResource(
                                    R.string.city_with_country,
                                    city.name,
                                    CountryNames.display(city.countryCode, locale),
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPick(city.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp),
                                fontSize = 16.sp,
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
