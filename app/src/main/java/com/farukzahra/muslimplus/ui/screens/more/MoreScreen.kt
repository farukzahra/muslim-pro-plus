package com.farukzahra.muslimplus.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.BuildConfig
import com.farukzahra.muslimplus.R

/** Os seis destinos da grade do Mais, na ordem em que aparecem. */
private data class MoreItem(
    val labelRes: Int,
    val iconRes: Int,
    val onClick: () -> Unit,
    val supporting: String? = null,
)

/**
 * Aba Mais como grade de dois cartoes por linha. Notificacoes e data hijri
 * ficam dentro de Configuracoes; aqui so os destinos de nivel superior.
 *
 * O cartao inteiro e clicavel com `role = Button` e o icone tem descricao, para
 * o TalkBack anunciar o destino mesmo sem ler o rotulo.
 */
@Composable
fun MoreScreen(
    onSettingsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onQuranDisplayClick: () -> Unit,
    onThemeClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val items = listOf(
        MoreItem(R.string.more_settings, R.drawable.ic_more_settings, onSettingsClick),
        MoreItem(R.string.more_language, R.drawable.ic_more_language, onLanguageClick),
        MoreItem(
            R.string.more_quran_display,
            R.drawable.ic_more_quran_display,
            onQuranDisplayClick,
        ),
        MoreItem(R.string.more_theme, R.drawable.ic_more_theme, onThemeClick),
        MoreItem(R.string.more_licenses, R.drawable.ic_more_licenses, onLicensesClick),
        MoreItem(
            R.string.more_about,
            R.drawable.ic_more_about,
            onAboutClick,
            supporting = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.tab_more),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                MoreCard(item)
            }
        }
    }
}

@Composable
private fun MoreCard(item: MoreItem) {
    val label = stringResource(item.labelRes)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .clickable(role = Role.Button, onClick = item.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    textAlign = TextAlign.Start,
                )
                item.supporting?.let { supporting ->
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
