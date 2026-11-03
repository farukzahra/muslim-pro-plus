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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.BuildConfig
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.update.UpdateCardState

/** Os destinos da grade do Mais, na ordem em que aparecem. */
private data class MoreItem(
    val labelRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val supporting: String? = null,
    val testTag: String? = null,
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
    updateCard: UpdateCardState = UpdateCardState.Unknown,
    updatePercent: Int? = null,
    onUpdateClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
) {
    var showDonate by remember { mutableStateOf(false) }
    val items = listOf(
        MoreItem(R.string.more_settings, Icons.Outlined.Settings, onSettingsClick),
        MoreItem(R.string.more_language, Icons.Outlined.Language, onLanguageClick),
        MoreItem(
            R.string.more_quran_display,
            Icons.Outlined.TextFields,
            onQuranDisplayClick,
        ),
        MoreItem(R.string.more_theme, Icons.Outlined.DarkMode, onThemeClick),
        MoreItem(R.string.more_licenses, Icons.Outlined.Gavel, onLicensesClick),
        MoreItem(
            R.string.more_about,
            Icons.Outlined.Info,
            onAboutClick,
            supporting = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        ),
        MoreItem(
            R.string.more_updates,
            Icons.Outlined.SystemUpdate,
            onClick = when (updateCard) {
                UpdateCardState.Available -> onUpdateClick
                UpdateCardState.ReadyToInstall -> onRestartClick
                else -> onCheckUpdate
            },
            supporting = updateStatus(updateCard, updatePercent),
            testTag = "more_updates",
        ),
        MoreItem(
            R.string.more_donate,
            Icons.Outlined.Favorite,
            onClick = { showDonate = true },
            supporting = stringResource(R.string.donate_supporting),
            testTag = "more_donate",
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

    if (showDonate) DonateDialog(onDismiss = { showDonate = false })
}

@Composable
private fun updateStatus(state: UpdateCardState, percent: Int?): String = when (state) {
    UpdateCardState.Unknown -> stringResource(R.string.update_status_check)
    UpdateCardState.UpToDate -> stringResource(R.string.update_status_current)
    UpdateCardState.Available -> stringResource(R.string.update_status_available)
    UpdateCardState.Downloading -> percent?.let {
        stringResource(R.string.update_status_downloading, it)
    } ?: stringResource(R.string.update_status_downloading_unknown)

    UpdateCardState.ReadyToInstall -> stringResource(R.string.update_status_ready)
}

@Composable
private fun MoreCard(item: MoreItem) {
    val label = stringResource(item.labelRes)
    val cardModifier = Modifier
        .fillMaxWidth()
        .height(124.dp)
    Card(
        modifier = (if (item.testTag != null) cardModifier.testTag(item.testTag) else cardModifier)
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
                imageVector = item.icon,
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
