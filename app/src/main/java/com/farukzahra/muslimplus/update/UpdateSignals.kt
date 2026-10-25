package com.farukzahra.muslimplus.update

/** O que o Play Informa sobre atualização instalável. Puro Kotlin, testável em JVM. */
enum class UpdateAvailability {
    Unknown,
    None,
    Available,
    Downloading,
    Downloaded,
}

data class UpdateSignals(
    val availability: UpdateAvailability = UpdateAvailability.Unknown,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
)

enum class UpdateDialogKind {
    Available,
    ReadyToRestart,
}

enum class UpdateCardState {
    Unknown,
    UpToDate,
    Available,
    Downloading,
    ReadyToInstall,
}

object UpdatePromptPolicy {
    /**
     * Dialogo automatico: oferta quando ha versao nova e o usuario ainda nao
     * dispensou; reinicio quando o download terminou e ele nao adiou.
     */
    fun dialogFor(
        signals: UpdateSignals,
        promptDismissed: Boolean,
        restartPostponed: Boolean,
    ): UpdateDialogKind? = when {
        signals.availability == UpdateAvailability.Downloaded && !restartPostponed ->
            UpdateDialogKind.ReadyToRestart

        signals.availability == UpdateAvailability.Available && !promptDismissed ->
            UpdateDialogKind.Available

        else -> null
    }

    fun cardFor(signals: UpdateSignals): UpdateCardState = when (signals.availability) {
        UpdateAvailability.Unknown -> UpdateCardState.Unknown
        UpdateAvailability.None -> UpdateCardState.UpToDate
        UpdateAvailability.Available -> UpdateCardState.Available
        UpdateAvailability.Downloading -> UpdateCardState.Downloading
        UpdateAvailability.Downloaded -> UpdateCardState.ReadyToInstall
    }

    /** So limpa o "agora nao" quando o Play confirma que nao ha nada pendente. */
    fun clearsDismissal(signals: UpdateSignals): Boolean =
        signals.availability == UpdateAvailability.None

    fun downloadPercent(signals: UpdateSignals): Int? {
        if (signals.totalBytes <= 0L) return null
        val percent = signals.bytesDownloaded * 100 / signals.totalBytes
        return percent.coerceIn(0L, 100L).toInt()
    }
}
