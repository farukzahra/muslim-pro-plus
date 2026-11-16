package com.farukzahra.muslimplus.ui.screens.more

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.farukzahra.muslimplus.AppLinks
import com.farukzahra.muslimplus.BuildConfig
import com.farukzahra.muslimplus.R
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Intent do Fale conosco. `ACTION_SENDTO` com esquema `mailto` garante que so
 * clientes de e-mail aparecem, sem apps de mensagem no meio.
 */
fun contactEmailIntent(subject: String, body: String, email: String = AppLinks.CONTACT_EMAIL): Intent =
    Intent(Intent.ACTION_SENDTO, ContactMessage.mailto(email, subject, body).toUri())

/**
 * Dados tecnicos que vao junto do texto do usuario. O dialogo avisa que eles
 * seguem na mensagem; servem para reproduzir um problema sem precisar perguntar.
 */
fun contactDetails(
    appName: String,
    versionName: String,
    versionCode: Int,
    device: String = Build.MODEL,
    android: String = Build.VERSION.RELEASE,
    language: String = Locale.getDefault().toLanguageTag(),
): List<String> = listOf(
    "$appName $versionName ($versionCode)",
    "Android $android · $device",
    language,
)

/**
 * Abre o app de e-mail com assunto e corpo prontos.
 *
 * Devolve `false` quando nao ha cliente de e-mail (ou ele nao abre), para o
 * dialogo mostrar o endereco e o botao de copiar em vez de perder a mensagem.
 */
fun sendContactEmail(context: Context, message: String): Boolean {
    val subject = context.getString(R.string.contact_subject, BuildConfig.VERSION_NAME)
    val details = contactDetails(
        appName = context.getString(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
    )
    val intent = contactEmailIntent(
        subject = subject,
        body = ContactMessage.body(message, details),
    )
    if (intent.resolveActivity(context.packageManager) == null) return false
    val chooser = Intent.createChooser(intent, context.getString(R.string.contact_send))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(chooser) }.isSuccess
}

/**
 * Dialogo do Fale conosco: campo multilinha simples (sem rich text, que
 * exigiria biblioteca e conversao para HTML sem ganho para uma sugestao).
 *
 * [onSend] existe para o teste E2E: recebe o texto e devolve se o envio
 * funcionou. Sem ele, abre o app de e-mail do aparelho.
 */
@Composable
fun ContactDialog(
    onDismiss: () -> Unit,
    onSend: ((String) -> Boolean)? = null,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val send = onSend ?: { message -> sendContactEmail(context, message) }
    var text by rememberSaveable { mutableStateOf("") }
    var failed by rememberSaveable { mutableStateOf(false) }
    val copied = stringResource(R.string.contact_copied)
    val canSend = ContactMessage.isValid(text)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("contact_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Outlined.MailOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.contact_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.contact_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = ContactMessage.limit(it)
                        failed = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_field"),
                    placeholder = { Text(stringResource(R.string.contact_hint)) },
                    minLines = 4,
                    maxLines = 7,
                    supportingText = {
                        Text(
                            text = stringResource(
                                R.string.contact_counter,
                                text.length,
                                ContactMessage.MAX_LENGTH,
                            ),
                            modifier = Modifier.testTag("contact_counter"),
                        )
                    },
                )
                Text(
                    text = stringResource(R.string.contact_details_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (failed) {
                    Text(
                        text = stringResource(R.string.contact_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("contact_failed"),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppLinks.CONTACT_EMAIL,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_email"),
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                "email",
                                                AppLinks.CONTACT_EMAIL,
                                            ),
                                        ),
                                    )
                                }
                                Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("contact_copy_email"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.contact_copy_email),
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSend,
                onClick = { if (send(text)) onDismiss() else failed = true },
                modifier = Modifier.testTag("contact_send"),
            ) {
                Text(stringResource(R.string.contact_send))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("contact_cancel"),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
