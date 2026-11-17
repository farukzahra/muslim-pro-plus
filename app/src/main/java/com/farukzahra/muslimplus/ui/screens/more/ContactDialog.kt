package com.farukzahra.muslimplus.ui.screens.more

import android.content.ClipData
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.launch

/**
 * Intent do fallback por e-mail. `ACTION_SENDTO` com esquema `mailto` garante
 * que so clientes de e-mail aparecem, sem apps de mensagem no meio.
 */
fun contactEmailIntent(subject: String, body: String, email: String = AppLinks.CONTACT_EMAIL): Intent =
    Intent(Intent.ACTION_SENDTO, ContactMessage.mailto(email, subject, body).toUri())

/**
 * Abre o app de e-mail com assunto e corpo prontos. Devolve `false` quando nao
 * ha cliente de e-mail (ou ele nao abre), para o dialogo mostrar o endereco e
 * o botao de copiar em vez de perder a mensagem.
 */
fun sendContactEmail(context: Context, message: String): Boolean {
    val details = contactDetails(
        appName = context.getString(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
    )
    val intent = contactEmailIntent(
        subject = context.getString(R.string.contact_subject, BuildConfig.VERSION_NAME),
        body = ContactMessage.body(message, details.signature()),
    )
    if (intent.resolveActivity(context.packageManager) == null) return false
    val chooser = Intent.createChooser(intent, context.getString(R.string.contact_send))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(chooser) }.isSuccess
}

/** Estados do envio direto pela API. */
private enum class SendState { Idle, Sending, Error }

/**
 * Dialogo do Fale conosco: campo multilinha simples, envio direto pelo
 * Web3Forms e fallback por e-mail quando a API falha ou nao ha app de e-mail.
 *
 * [sender] e [sendEmail] existem para o teste trocar rede e intent por fakes.
 * Sem eles, usa a API real e o cliente de e-mail do aparelho.
 */
@Composable
fun ContactDialog(
    onDismiss: () -> Unit,
    onSent: () -> Unit = onDismiss,
    sender: ContactSender? = null,
    sendEmail: ((String) -> Boolean)? = null,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val activeSender = sender ?: remember { Web3FormsContactSender(BuildConfig.WEB3FORMS_ACCESS_KEY) }
    val emailSender = sendEmail ?: { message -> sendContactEmail(context, message) }
    var text by rememberSaveable { mutableStateOf("") }
    var sendState by remember { mutableStateOf(SendState.Idle) }
    var emailMissing by rememberSaveable { mutableStateOf(false) }
    val copied = stringResource(R.string.contact_copied)
    val sending = sendState == SendState.Sending
    val failed = sendState == SendState.Error
    val canSend = ContactMessage.isValid(text) && !sending

    fun submit() {
        if (sending) return
        emailMissing = false
        sendState = SendState.Sending
        val details = contactDetails(
            appName = context.getString(R.string.app_name),
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
        )
        val payload = ContactRequest.json(
            details = details,
            accessKey = BuildConfig.WEB3FORMS_ACCESS_KEY,
            subject = context.getString(R.string.contact_subject, BuildConfig.VERSION_NAME),
            message = ContactMessage.body(text, details.signature()),
        )
        scope.launch {
            if (activeSender.send(payload)) onSent() else sendState = SendState.Error
        }
    }

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
                        if (failed) sendState = SendState.Idle
                        emailMissing = false
                    },
                    enabled = !sending,
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
                        text = stringResource(R.string.contact_send_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("contact_send_failed"),
                    )
                    TextButton(
                        onClick = { if (emailSender(text)) onSent() else emailMissing = true },
                        modifier = Modifier.testTag("contact_use_email"),
                    ) {
                        Text(stringResource(R.string.contact_use_email))
                    }
                }
                if (emailMissing) {
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
                onClick = { submit() },
                modifier = Modifier.testTag("contact_send"),
            ) {
                Text(
                    text = when {
                        sending -> stringResource(R.string.contact_sending)
                        failed -> stringResource(R.string.contact_retry)
                        else -> stringResource(R.string.contact_send)
                    },
                )
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

/** Confirmacao depois do envio direto; substitui o antigo "abriu o e-mail". */
@Composable
fun ContactSentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("contact_sent_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.contact_sent_title)) },
        text = {
            Text(
                text = stringResource(R.string.contact_sent_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("contact_sent_ok"),
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
    )
}
