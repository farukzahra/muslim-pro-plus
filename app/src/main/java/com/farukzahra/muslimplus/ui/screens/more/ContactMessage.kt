package com.farukzahra.muslimplus.ui.screens.more

import java.nio.charset.StandardCharsets

/**
 * Regras do "Fale conosco", sem Android: valida o texto, monta o corpo com os
 * dados do aparelho e codifica o `mailto:`. Tudo testado em JVM.
 *
 * Sem backend por decisao do projeto: a mensagem sai pelo app de e-mail do
 * aparelho, com assunto e corpo prontos, e o usuario revisa antes de enviar.
 */
object ContactMessage {
    /** Cabe em qualquer cliente de e-mail e evita uma URI gigante. */
    const val MAX_LENGTH = 2000

    /** Abaixo disso e clique por engano, nao sugestao. */
    const val MIN_LENGTH = 3

    /** Separador entre a mensagem e os dados tecnicos (padrao de assinatura). */
    const val DETAILS_SEPARATOR = "\n\n-- \n"

    private const val HEX = "0123456789ABCDEF"

    /** `trim` e quebra de linha sempre como `\n`, venha de teclado ou de colagem. */
    fun normalize(raw: String): String = raw.replace("\r\n", "\n").replace('\r', '\n').trim()

    fun isValid(raw: String): Boolean = normalize(raw).length >= MIN_LENGTH

    /** Corta em [MAX_LENGTH] sem deixar o campo passar do limite. */
    fun limit(raw: String): String = raw.take(MAX_LENGTH)

    /**
     * Mensagem + assinatura tecnica. Cabecalho vazio devolve so a mensagem,
     * para o envio nunca terminar em separador solto.
     */
    fun body(message: String, header: List<String> = emptyList()): String {
        val text = normalize(message)
        val details = header.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
        return if (details.isEmpty()) text else text + DETAILS_SEPARATOR + details
    }

    /**
     * `mailto:` com assunto e corpo percent-encoded em UTF-8. Espaco vira `%20`
     * (nao `+`, que muitos clientes mostram literalmente) e quebra de linha
     * vira `%0A`.
     */
    fun mailto(email: String, subject: String, body: String): String =
        "mailto:${email.trim()}?subject=${encode(subject)}&body=${encode(body)}"

    fun encode(value: String): String {
        val out = StringBuilder()
        value.toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            val code = byte.toInt() and 0xFF
            if (code < 0x80 && isUnreserved(code.toChar())) {
                out.append(code.toChar())
            } else {
                out.append('%')
                out.append(HEX[code shr 4])
                out.append(HEX[code and 0x0F])
            }
        }
        return out.toString()
    }

    private fun isUnreserved(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~"
}
