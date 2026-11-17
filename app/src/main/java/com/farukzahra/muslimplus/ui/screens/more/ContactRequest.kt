package com.farukzahra.muslimplus.ui.screens.more

/**
 * Dados do Fale conosco, sem Android: o chamador passa versao, modelo e idioma.
 * Usado tanto na assinatura do `mailto:` quanto nos campos do JSON.
 */
data class ContactDetails(
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val android: String,
    val device: String,
    val language: String,
) {
    /** Assinatura em texto que fecha a mensagem. */
    fun signature(): List<String> = listOf(
        "$appName $versionName ($versionCode)",
        "Android $android · $device",
        language,
    )
}

/**
 * Corpo do POST para o Web3Forms. JSON escrito a mao para nao puxar
 * serializador so por causa de dez campos; o escape tem teste proprio em JVM.
 */
object ContactRequest {
    const val FROM_NAME = "Muslim Plus"

    fun json(
        details: ContactDetails,
        accessKey: String,
        subject: String,
        message: String,
    ): String = buildString {
        append('{')
        field("access_key", accessKey)
        append(',')
        field("subject", subject)
        append(',')
        field("from_name", FROM_NAME)
        append(',')
        field("message", message)
        append(',')
        field("app", details.appName)
        append(',')
        field("version", "${details.versionName} (${details.versionCode})")
        append(',')
        field("android", details.android)
        append(',')
        field("device", details.device)
        append(',')
        field("language", details.language)
        append(',')
        append("\"botcheck\":false")
        append('}')
    }

    /**
     * Escape de string JSON: aspas, barra invertida, quebras de linha, tab e
     * controles. Acento continua literal, que e UTF-8 valido em JSON.
     */
    fun escape(value: String): String {
        val out = StringBuilder(value.length + 16)
        value.forEach { char ->
            when (char) {
                '"' -> out.append("\\\"")
                '\\' -> out.append("\\\\")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                else -> if (char < ' ') {
                    out.append("\\u")
                    out.append(char.code.toString(16).padStart(4, '0'))
                } else {
                    out.append(char)
                }
            }
        }
        return out.toString()
    }

    private fun StringBuilder.field(name: String, value: String) {
        append('"')
        append(name)
        append("\":\"")
        append(escape(value))
        append('"')
    }
}
