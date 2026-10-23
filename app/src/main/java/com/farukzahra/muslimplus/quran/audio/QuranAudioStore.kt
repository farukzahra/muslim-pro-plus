package com.farukzahra.muslimplus.quran.audio

import android.content.Context
import com.farukzahra.muslimplus.quran.VerseRef
import java.io.File
import java.io.IOException

/** Explicit downloads, isolated by voice; never subject to playback cache eviction. */
class QuranAudioStore(
    private val root: File,
    private val source: QuranAudioSource = EveryAyahSource,
    @Suppress("UNUSED_PARAMETER") limitBytes: Long = AudioCache.LIMIT_BYTES,
) {
    constructor(context: Context, source: QuranAudioSource = EveryAyahSource) :
        this(File(context.filesDir, "audio"), source)

    private val voiceRoot: File
        get() = safe(File(root, source.id))

    init { require(source.id.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*"))) }

    private fun safe(file: File): File {
        require(file.canonicalPath.startsWith(root.canonicalPath + File.separator))
        val relative = root.absoluteFile.normalize().toPath().relativize(file.absoluteFile.normalize().toPath())
        require(file.canonicalFile == File(root.canonicalFile, relative.toString()))
        return file
    }

    fun surahDir(surah: Int): File {
        require(surah in 1..114)
        return safe(File(voiceRoot, source.pad(surah)))
    }

    fun fileFor(surah: Int, ayah: Int): File {
        require(VerseRef.of(surah, ayah) != null)
        return safe(File(surahDir(surah), source.fileNameFor(surah, ayah)))
    }

    fun partFor(surah: Int, ayah: Int): File = safe(File(fileFor(surah, ayah).path + ".part"))

    /** Move only complete legacy Alafasy verses; failed moves leave the original intact. */
    private fun migrate(surah: Int, ayah: Int) {
        if (source.id != "alafasy-128") return
        val legacy = safe(File(File(root, source.pad(surah)), source.fileNameFor(surah, ayah)))
        val target = fileFor(surah, ayah)
        if (!target.exists() && legacy.isFile && legacy.length() > 0) {
            prepareDir(surah)
            legacy.renameTo(target)
        }
    }

    fun localAudio(surah: Int, ayah: Int): File? {
        migrate(surah, ayah)
        return fileFor(surah, ayah).takeIf { it.isFile && it.length() > 0 }
    }

    fun savedVerses(surah: Int, verseCount: Int): Int {
        require(surah in 1..114 && verseCount in 0..VerseRef.MAX_AYAH)
        return (1..verseCount).count { localAudio(surah, it) != null }
    }

    fun isSurahSaved(surah: Int, verseCount: Int): Boolean =
        verseCount > 0 && savedVerses(surah, verseCount) == verseCount

    fun touch(surah: Int, ayah: Int) { localAudio(surah, ayah)?.setLastModified(System.currentTimeMillis()) }

    fun prepareDir(surah: Int) {
        val dir = surahDir(surah)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Cannot create audio directory")
    }

    @Deprecated("Explicit downloads are preserved; streaming cache owns eviction")
    fun evict(@Suppress("UNUSED_PARAMETER") keepSurah: Int? = null): List<String> = emptyList()

    private fun migrateAll() {
        if (source.id == "alafasy-128") for (surah in 1..114) {
            val dir = safe(File(root, source.pad(surah)))
            if (dir.isDirectory) for (ayah in 1..QuranReciters.verseCounts[surah - 1]) migrate(surah, ayah)
        }
    }

    fun totalBytes(): Long {
        migrateAll()
        return voiceRoot.walkTopDown().filter { it.isFile && it.extension == "mp3" }
            .sumOf { safe(it).length() }
    }

    fun deleteSurah(surah: Int) {
        if (source.id == "alafasy-128") for (ayah in 1..VerseRef.MAX_AYAH) migrate(surah, ayah)
        deleteOwned(surahDir(surah))
    }

    fun deleteAll() { migrateAll(); deleteOwned(voiceRoot) }

    private fun deleteOwned(file: File) {
        safe(file)
        if (file.isDirectory) file.listFiles()?.forEach { deleteOwned(it) }
        if (file.exists() && !file.delete()) throw IOException("Cannot delete audio file")
    }
}
