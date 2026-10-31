package com.farukzahra.muslimplus.settings

/**
 * Preferências do leitor do Alcorão. Valores são guardados como texto estável
 * no DataStore para não depender da ordem do enum em versões futuras.
 */
enum class ReaderTheme(val id: String) {
    MODERN("modern"),
    PAPER("paper"),
    DARK("dark"),
    ;

    companion object {
        fun fromId(id: String?): ReaderTheme = entries.firstOrNull { it.id == id } ?: MODERN
    }
}

enum class ReaderFont(val id: String, val scale: Float) {
    SMALL("small", 1.0f),
    MEDIUM("medium", 1.15f),
    LARGE("large", 1.3f),
    XLARGE("xlarge", 1.45f),
    ;

    companion object {
        fun fromId(id: String?): ReaderFont = entries.firstOrNull { it.id == id } ?: MEDIUM
    }
}

enum class ReaderMode(val id: String) {
    LIST("list"),
    PAGE("page"),
    ;

    companion object {
        fun fromId(id: String?): ReaderMode = entries.firstOrNull { it.id == id } ?: LIST
    }
}

object ReaderPrefs {
    val themes: List<ReaderTheme> = ReaderTheme.entries
    val fonts: List<ReaderFont> = ReaderFont.entries
    val modes: List<ReaderMode> = ReaderMode.entries

    /** Velocidades aceitas pelo mini-player; fora disso volta para 1×. */
    val speeds: List<Float> = listOf(0.75f, 1.0f, 1.25f, 1.5f)

    const val DEFAULT_SPEED = 1.0f

    fun speed(value: Float?): Float =
        speeds.firstOrNull { kotlin.math.abs(it - (value ?: DEFAULT_SPEED)) < 0.01f }
            ?: DEFAULT_SPEED

    /** Próxima velocidade no ciclo do botão, sempre dentro da lista. */
    fun nextSpeed(current: Float): Float {
        val index = speeds.indexOf(speed(current))
        return speeds[(index + 1) % speeds.size]
    }

    /** "1x", "0.75x", sem depender de locale para o separador. */
    fun speedLabel(value: Float): String {
        val clean = speed(value)
        val number = if (clean == clean.toInt().toFloat()) {
            clean.toInt().toString()
        } else {
            clean.toString().trimEnd('0').trimEnd('.')
        }
        return "${number}x"
    }
}
