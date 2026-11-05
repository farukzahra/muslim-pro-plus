package com.farukzahra.muslimplus

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.quran.audio.QuranReciters
import com.farukzahra.muslimplus.quran.audio.QuranAudioStore
import androidx.compose.ui.test.performScrollTo
import com.farukzahra.muslimplus.quran.audio.QuranStreamingCache
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import android.net.Uri
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@androidx.media3.common.util.UnstableApi
@RunWith(AndroidJUnit4::class)
class AudioStorageScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun selectionPersistsAndCacheCleaningPreservesDownloads() {
        runBlocking { ServiceLocator.settings.setOnboardingDone() }
        compose.activityRule.scenario.recreate()
        val context = compose.activity
        compose.onNodeWithText(context.getString(R.string.tab_more)).performClick()
        compose.onNodeWithText(context.getString(R.string.more_settings)).performClick()
        compose.onNodeWithText(context.getString(R.string.audio_storage_title)).performScrollTo().performClick()
        val originalReciter = runBlocking { ServiceLocator.settings.quranReciterId.first() }
        val reciter = QuranReciters.all[1]
        // O recitador agora fica no cartão do topo; a lista abre em uma folha.
        compose.waitUntil { compose.onAllNodesWithTag("reciter_change").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("reciter_change").performClick()
        compose.waitUntil {
            compose.onAllNodesWithTag("reciter_${reciter.id}").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("reciter_${reciter.id}").performScrollTo().performClick()
        compose.waitUntil { runBlocking { ServiceLocator.settings.quranReciterId.first() } == reciter.id }
        val offline = QuranAudioStore(context, reciter).let { it.prepareDir(1); it.fileFor(1, 1) }
        val previous = offline.takeIf { it.exists() }?.readBytes()
        offline.writeBytes(byteArrayOf(1, 2, 3))
        val source = QuranStreamingCache.factory(context, DataSource.Factory { ByteArrayDataSource(ByteArray(1024) { 1 }) }).createDataSource()
        try {
            source.open(DataSpec(Uri.parse("https://everyayah.com/data/test-ui-cache.mp3")))
            val buffer = ByteArray(1024)
            while (source.read(buffer, 0, buffer.size) != -1) { }
        } finally { source.close() }
        check(QuranStreamingCache.bytes(context) > 0)
        try {
            compose.onNodeWithTag("clear_audio_cache").performScrollTo().performClick()
            compose.waitUntil { QuranStreamingCache.bytes(context) == 0L }
            check(offline.readBytes().contentEquals(byteArrayOf(1, 2, 3)))
            compose.activityRule.scenario.recreate()
            compose.waitUntil { runBlocking { ServiceLocator.settings.quranReciterId.first() } == reciter.id }
        } finally {
            if (previous == null) offline.delete() else offline.writeBytes(previous)
            runBlocking { ServiceLocator.settings.setQuranReciterId(originalReciter) }
        }
    }
}


