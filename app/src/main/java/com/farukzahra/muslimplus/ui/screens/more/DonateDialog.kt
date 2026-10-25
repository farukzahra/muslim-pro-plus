package com.farukzahra.muslimplus.ui.screens.more

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ui.components.PixCard
import com.farukzahra.muslimplus.ui.screens.settings.PIX_KEY

/** Cartão de apoio: explica que o app é sempre gratuito e oferece a chave Pix. */
@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copied = stringResource(R.string.about_pix_copied)

    fun copy() {
        clipboard.setText(AnnotatedString(PIX_KEY))
        Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("donate_dialog"),
        title = { Text(stringResource(R.string.more_donate)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.donate_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                PixCard(
                    pixKey = PIX_KEY,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = stringResource(R.string.about_pix_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = ::copy) { Text(stringResource(R.string.donate_copy)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}
