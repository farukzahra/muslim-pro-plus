package com.farukzahra.muslimplus.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R

const val BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/farukzahra"

/**
 * Duas formas de apoiar: Pix (copia a chave) e Buy me a coffee (abre o site).
 * Cada uma e um cartao com icone, titulo e acao visivel.
 */
@Composable
fun SupportOptions(
    pixKey: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copied = stringResource(R.string.about_pix_copied)

    fun copyPix() {
        clipboard.setText(AnnotatedString(pixKey))
        Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
    }

    fun openCoffee() {
        openUrl(context, BUY_ME_A_COFFEE_URL)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SupportMethodCard(
            iconRes = R.drawable.ic_pix,
            title = stringResource(R.string.donate_pix_title),
            supporting = pixKey,
            monospace = true,
            onClick = ::copyPix,
            testTag = "donate_pix_card",
            trailing = {
                IconButton(
                    onClick = ::copyPix,
                    modifier = Modifier.testTag("donate_pix_copy"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pix_copy),
                        contentDescription = stringResource(R.string.donate_copy),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
        SupportMethodCard(
            iconRes = R.drawable.ic_coffee,
            title = stringResource(R.string.donate_bmac_title),
            supporting = BUY_ME_A_COFFEE_URL.removePrefix("https://"),
            onClick = ::openCoffee,
            testTag = "donate_bmac_card",
            trailing = {
                TextButton(
                    onClick = ::openCoffee,
                    modifier = Modifier.testTag("donate_bmac_action"),
                ) {
                    Text(stringResource(R.string.donate_bmac_action))
                }
            },
        )
    }
}

@Composable
private fun SupportMethodCard(
    iconRes: Int,
    title: String,
    supporting: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
    trailing: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            trailing()
        }
    }
}
