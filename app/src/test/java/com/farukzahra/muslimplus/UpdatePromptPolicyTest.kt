package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.update.UpdateAvailability
import com.farukzahra.muslimplus.update.UpdateCardState
import com.farukzahra.muslimplus.update.UpdateDialogKind
import com.farukzahra.muslimplus.update.UpdatePromptPolicy
import com.farukzahra.muslimplus.update.UpdateSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePromptPolicyTest {
    @Test
    fun offersDialogOnlyWhenUpdateIsAvailableAndNotDismissed() {
        val available = UpdateSignals(UpdateAvailability.Available)
        assertEquals(UpdateDialogKind.Available, UpdatePromptPolicy.dialogFor(available, false, false))
        assertNull(UpdatePromptPolicy.dialogFor(available, true, false))
    }

    @Test
    fun downloadedAsksToRestartUnlessPostponed() {
        val downloaded = UpdateSignals(UpdateAvailability.Downloaded)
        assertEquals(UpdateDialogKind.ReadyToRestart, UpdatePromptPolicy.dialogFor(downloaded, true, false))
        assertNull(UpdatePromptPolicy.dialogFor(downloaded, true, true))
    }

    @Test
    fun noDialogWhileDownloadingOrUpToDate() {
        assertNull(
            UpdatePromptPolicy.dialogFor(UpdateSignals(UpdateAvailability.Downloading), false, false),
        )
        assertNull(UpdatePromptPolicy.dialogFor(UpdateSignals(UpdateAvailability.None), false, false))
    }

    @Test
    fun cardReflectsEveryAvailability() {
        assertEquals(UpdateCardState.Unknown, UpdatePromptPolicy.cardFor(UpdateSignals()))
        assertEquals(UpdateCardState.UpToDate, UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.None)))
        assertEquals(UpdateCardState.Available, UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Available)))
        assertEquals(
            UpdateCardState.Downloading,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Downloading)),
        )
        assertEquals(
            UpdateCardState.ReadyToInstall,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Downloaded)),
        )
    }

    @Test
    fun percentIsClampedAndUnknownWithoutTotal() {
        assertNull(UpdatePromptPolicy.downloadPercent(UpdateSignals(UpdateAvailability.Downloading)))
        assertEquals(
            42,
            UpdatePromptPolicy.downloadPercent(
                UpdateSignals(UpdateAvailability.Downloading, bytesDownloaded = 42, totalBytes = 100),
            ),
        )
        assertEquals(
            100,
            UpdatePromptPolicy.downloadPercent(
                UpdateSignals(UpdateAvailability.Downloading, bytesDownloaded = 500, totalBytes = 100),
            ),
        )
    }

    @Test
    fun dismissalClearsOnlyWhenPlaySaysThereIsNothingPending() {
        assertTrue(UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.None)))
        assertFalse(UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.Available)))
    }
}
