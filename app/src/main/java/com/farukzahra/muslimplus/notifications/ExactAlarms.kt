package com.farukzahra.muslimplus.notifications

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Alarme exato depende de permissao propria desde o Android 12. Abaixo disso
 * `setExactAndAllowWhileIdle` sempre funciona.
 */
object ExactAlarms {
    fun isAvailable(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /**
     * Tela de permissao do app. Em versoes sem a permissao, cai nos detalhes do
     * app para o usuario nao ficar sem acao nenhuma.
     */
    fun requestIntent(context: Context): Intent {
        val packageUri = Uri.fromParts("package", context.packageName, null)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
        }
    }
}
