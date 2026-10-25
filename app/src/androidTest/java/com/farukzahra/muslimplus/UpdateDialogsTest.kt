package com.farukzahra.muslimplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme
import com.farukzahra.muslimplus.ui.update.UpdateDialogs
import com.farukzahra.muslimplus.update.UpdateDialogKind
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateDialogsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun availableDialogStartsUpdateOrDismisses() {
        var updated = false
        var dismissed = false
        compose.setContent {
            MuslimPlusTheme {
                UpdateDialogs(
                    dialog = UpdateDialogKind.Available,
                    onUpdate = { updated = true },
                    onLater = { dismissed = true },
                    onRestart = {},
                    onPostpone = {},
                )
            }
        }

        compose.onNodeWithTag("update_dialog").assertIsDisplayed()
        compose.onNodeWithTag("update_dialog_update").performClick()
        assertTrue(updated)

        compose.onNodeWithTag("update_dialog_later").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun readyDialogRestartsOrPostpones() {
        var restarted = false
        var postponed = false
        compose.setContent {
            MuslimPlusTheme {
                UpdateDialogs(
                    dialog = UpdateDialogKind.ReadyToRestart,
                    onUpdate = {},
                    onLater = {},
                    onRestart = { restarted = true },
                    onPostpone = { postponed = true },
                )
            }
        }

        compose.onNodeWithTag("restart_dialog").assertIsDisplayed()
        compose.onNodeWithTag("restart_dialog_restart").performClick()
        assertTrue(restarted)

        compose.onNodeWithTag("restart_dialog_later").performClick()
        assertTrue(postponed)
    }
}
