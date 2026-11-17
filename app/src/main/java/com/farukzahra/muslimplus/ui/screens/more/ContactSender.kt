package com.farukzahra.muslimplus.ui.screens.more

import android.os.Build
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Envio da sugestao. Interface existe para o teste trocar a rede por um fake. */
fun interface ContactSender {
    suspend fun send(payload: String): Boolean
}

/**
 * POST no Web3Forms. Sucesso so com HTTP 200 e `"success": true` no corpo;
 * qualquer excecao, timeout ou resposta estranha vira `false` e o dialogo
 * mostra o erro com "Tentar de novo" e o fallback por e-mail.
 *
 * A chave e publica por design (vai dentro do APK); ela vem de
 * `local.properties`/`WEB3FORMS_ACCESS_KEY` e entra em `BuildConfig`. Sem
 * chave, [send] devolve `false` em vez de bater na API.
 */
class Web3FormsContactSender(
    private val accessKey: String,
    private val endpoint: String = ENDPOINT,
) : ContactSender {
    override suspend fun send(payload: String): Boolean = withContext(Dispatchers.IO) {
        if (accessKey.isBlank()) return@withContext false
        runCatching {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            connection.disconnect()
            code == HttpURLConnection.HTTP_OK && SUCCESS.containsMatchIn(body)
        }.getOrDefault(false)
    }

    companion object {
        const val ENDPOINT = "https://api.web3forms.com/submit"
        const val TIMEOUT_MS = 10_000

        /** Web3Forms responde `{"success":true,...}`; espaco e ordem variam. */
        private val SUCCESS = Regex("\"success\"\\s*:\\s*true")
    }
}

/**
 * Dados tecnicos do envio. Defaults do aparelho ficam aqui, entao o dialogo e o
 * teste passam apenas o que querem controlar.
 */
fun contactDetails(
    appName: String,
    versionName: String,
    versionCode: Int,
    device: String = Build.MODEL,
    android: String = Build.VERSION.RELEASE,
    language: String = Locale.getDefault().toLanguageTag(),
): ContactDetails = ContactDetails(
    appName = appName,
    versionName = versionName,
    versionCode = versionCode,
    android = android,
    device = device,
    language = language,
)
