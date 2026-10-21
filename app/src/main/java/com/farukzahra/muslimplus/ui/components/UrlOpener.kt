package com.farukzahra.muslimplus.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Abre [url] no navegador do aparelho.
 *
 * Devolve `false` quando nenhum app resolve o link (aparelho sem navegador,
 * perfil restrito, URL invalida) em vez de estourar `ActivityNotFoundException`
 * dentro de um clique: as telas de Licencas e Privacidade mostram o endereco
 * para copiar nesse caso.
 */
fun openUrl(context: Context, url: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(intent) }.isSuccess
}
