package com.farukzahra.muslimplus.settings

/** Tela em que o app abre depois de ler a flag de primeira execucao. */
enum class StartScreen { ONBOARDING, HOME }

/**
 * Regra pura do onboarding, sem Android, para ser testada na JVM.
 *
 * A flag `onboarding_done` e nullable de proposito: `null` significa "nunca
 * rodou" e e o unico caso que abre o onboarding. Quem pula tambem grava a
 * flag, porque pular e uma escolha e nao deve reaparecer na proxima abertura.
 */
object Onboarding {
    /** Chave no DataStore, compartilhada com [SettingsRepository]. */
    const val DONE_KEY = "onboarding_done"

    fun startScreen(done: Boolean?): StartScreen =
        if (done == true) StartScreen.HOME else StartScreen.ONBOARDING

    fun shouldShow(done: Boolean?): Boolean = startScreen(done) == StartScreen.ONBOARDING
}
