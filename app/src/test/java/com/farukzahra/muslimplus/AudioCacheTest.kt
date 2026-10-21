package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.quran.audio.AudioCache
import com.farukzahra.muslimplus.quran.audio.CachedAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regra LRU pura: nada de rede, nada de Android. [CachedAudio.lastUsedMs] e a
 * unica nocao de "antigo" que o cache conhece.
 */
class AudioCacheTest {
    private fun entry(id: String, bytes: Long, lastUsedMs: Long) =
        CachedAudio(id = id, bytes = bytes, lastUsedMs = lastUsedMs)

    @Test
    fun belowTheLimitRemovesNothing() {
        val sizes = listOf(
            entry("a", 40, 1),
            entry("b", 40, 2),
        )
        assertTrue(AudioCache.planEviction(sizes, limitBytes = 100).isEmpty())
    }

    @Test
    fun exactlyAtTheLimitRemovesNothing() {
        val sizes = listOf(entry("a", 60, 1), entry("b", 40, 2))
        assertTrue(AudioCache.planEviction(sizes, limitBytes = 100).isEmpty())
    }

    @Test
    fun aboveTheLimitRemovesTheOldestUntilItFits() {
        val sizes = listOf(
            entry("newest", 40, 300),
            entry("oldest", 40, 100),
            entry("middle", 40, 200),
        )
        // 120 > 100: tira o mais antigo (40) e o excedente zera.
        assertEquals(listOf("oldest"), AudioCache.planEviction(sizes, limitBytes = 100))
    }

    @Test
    fun keepsRemovingWhileStillAboveTheLimit() {
        val sizes = listOf(
            entry("a", 50, 1),
            entry("b", 50, 2),
            entry("c", 50, 3),
            entry("d", 50, 4),
        )
        // 200 com limite 100: precisa sair 100, ou seja os dois mais antigos.
        assertEquals(listOf("a", "b"), AudioCache.planEviction(sizes, limitBytes = 100))
    }

    @Test
    fun fileBiggerThanTheLimitGoesAwayAlone() {
        val sizes = listOf(entry("huge", 500, 1))
        assertEquals(listOf("huge"), AudioCache.planEviction(sizes, limitBytes = 100))
    }

    @Test
    fun emptyCacheIsNeverEvicted() {
        assertTrue(AudioCache.planEviction(emptyList(), limitBytes = 0).isEmpty())
    }

    @Test
    fun sameTimestampFallsBackToStableIdOrder() {
        val sizes = listOf(
            entry("b", 40, 1),
            entry("a", 40, 1),
            entry("c", 40, 1),
        )
        assertEquals(listOf("a"), AudioCache.planEviction(sizes, limitBytes = 100))
    }

    @Test
    fun defaultLimitIs300Megabytes() {
        assertEquals(300L * 1024 * 1024, AudioCache.LIMIT_BYTES)
    }
}
