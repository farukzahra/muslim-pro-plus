package com.farukzahra.muslimplus.ui.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.update.UpdateDialogKind

/** Dialogos do fluxo de atualizacao flexivel do Play. */
@Composable
fun UpdateDialogs(
    dialog: UpdateDialogKind?,
    onUpdate: () -> Unit,
    onLater: () -> Unit,
    onRestart: () -> Unit,
    onPostpone: () -> Unit,
) {
    when (dialog) {
        UpdateDialogKind.Available -> AlertDialog(
            onDismissRequest = onLater,
            modifier = Modifier.testTag("update_dialog"),
            title = { Text(stringResource(R.string.update_available_title)) },
            text = { Text(stringResource(R.string.update_available_body)) },
            confirmButton = {
                TextButton(
                    onClick = onUpdate,
                    modifier = Modifier.testTag("update_dialog_update"),
                ) {
                    Text(stringResource(R.string.update_now))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onLater,
                    modifier = Modifier.testTag("update_dialog_later"),
                ) {
                    Text(stringResource(R.string.update_later))
                }
            },
        )

        UpdateDialogKind.ReadyToRestart -> AlertDialog(
            onDismissRequest = onPostpone,
            modifier = Modifier.testTag("restart_dialog"),
            title = { Text(stringResource(R.string.update_ready_title)) },
            text = { Text(stringResource(R.string.update_ready_body)) },
            confirmButton = {
                TextButton(
                    onClick = onRestart,
                    modifier = Modifier.testTag("restart_dialog_restart"),
                ) {
                    Text(stringResource(R.string.update_restart))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onPostpone,
                    modifier = Modifier.testTag("restart_dialog_later"),
                ) {
                    Text(stringResource(R.string.update_restart_later))
                }
            },
        )

        null -> Unit
    }
}
