package com.farukzahra.muslimplus.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.farukzahra.muslimplus.adhan.AdhanMigrationPlan
import com.farukzahra.muslimplus.adhan.AdhanOverrides
import com.farukzahra.muslimplus.adhan.AdhanPolicy
import com.farukzahra.muslimplus.adhan.AdhanSound
import com.farukzahra.muslimplus.location.CityCatalog
import com.farukzahra.muslimplus.prayer.Prayer
import com.farukzahra.muslimplus.prayer.PrayerOffsets
import com.farukzahra.muslimplus.prayer.PrayerReminder
import java.time.DayOfWeek
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
    private val cities: CityCatalog = CityCatalog.fromAsset(context),
) : com.farukzahra.muslimplus.update.UpdatePromptStore {
    private val quranReciterKey = stringPreferencesKey("quran_reciter_id")
    private val cityKey = stringPreferencesKey("city_id")
    private val methodKey = stringPreferencesKey("prayer_method")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val quranModesKey = stringSetPreferencesKey("quran_modes")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val prayerOffsetsKey = stringPreferencesKey("prayer_offsets")
    private val hijriOffsetKey = intPreferencesKey("hijri_offset")
    private val onboardingDoneKey = booleanPreferencesKey(Onboarding.DONE_KEY)
    private val updatePromptDismissedKey = booleanPreferencesKey("update_prompt_dismissed")
    private val dailyVerseVisibleKey = booleanPreferencesKey("daily_verse_visible")
    private val adhanPrayersKey = stringSetPreferencesKey("adhan_prayers")
    private val adhanSoundKey = stringPreferencesKey("adhan_sound")
    private val adhanFajrSoundKey = stringPreferencesKey("adhan_fajr_sound")
    private val adhanFajrOverrideKey = booleanPreferencesKey("adhan_fajr_override")
    private val adhanFajrNoticeKey = booleanPreferencesKey("adhan_fajr_migration_notice")
    private val adhanRespectSilentKey = booleanPreferencesKey("adhan_respect_silent")
    private val adhanReminderKey = intPreferencesKey("adhan_reminder_minutes")
    private val adhanSoundByPrayerKey = stringPreferencesKey("adhan_sound_by_prayer")
    private val adhanReminderByPrayerKey = stringPreferencesKey("adhan_reminder_by_prayer")
    private val adhanAlarmByPrayerKey = stringPreferencesKey("adhan_alarm_by_prayer")
    private val adhanRepeatsByPrayerKey = stringPreferencesKey("adhan_repeats_by_prayer")
    private val quranSetupDoneKey = booleanPreferencesKey("quran_setup_done")
    private val quranGoalMinutesKey = intPreferencesKey("quran_goal_minutes")
    private val readerThemeKey = stringPreferencesKey("reader_theme")
    private val readerFontKey = stringPreferencesKey("reader_font")
    private val readerModeKey = stringPreferencesKey("reader_mode")
    private val readerKeepScreenOnKey = booleanPreferencesKey("reader_keep_screen_on")
    private val quranSpeedKey = stringPreferencesKey("quran_speed")

    /**
     * Cidade escolhida ou, sem escolha (primeira execucao), a maior cidade do
     * fuso do aparelho. Id salvo que o catalogo nao conhece (formato antigo)
     * tambem cai no padrao do fuso. Sem cidade no fuso, a primeira do catalogo.
     */
    val cityId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[cityKey]?.let(cities::byIdOrNull)?.id ?: defaultCityId()
    }
    val quranReciterId: Flow<String> = context.dataStore.data.map { it[quranReciterKey] ?: com.farukzahra.muslimplus.quran.audio.QuranReciters.default.id }
    val methodName: Flow<String> = context.dataStore.data.map { it[methodKey] ?: "MWL" }
    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[notificationsKey] ?: true }
    val adhanPrayers: Flow<Set<Prayer>> =
        context.dataStore.data.map { AdhanPolicy.sanitize(it[adhanPrayersKey]) }
    val adhanSoundId: Flow<String> =
        context.dataStore.data.map { it[adhanSoundKey] ?: AdhanSound.default.id }
    val adhanFajrSoundId: Flow<String> =
        context.dataStore.data.map { it[adhanFajrSoundKey] ?: AdhanSound.defaultFajr.id }
    val adhanFajrOverride: Flow<Boolean> =
        context.dataStore.data.map { it[adhanFajrOverrideKey] ?: true }
    val adhanFajrNotice: Flow<Boolean> =
        context.dataStore.data.map { it[adhanFajrNoticeKey] ?: false }
    val adhanRespectSilent: Flow<Boolean> =
        context.dataStore.data.map { it[adhanRespectSilentKey] ?: false }
    val dailyVerseVisible: Flow<Boolean> =
        context.dataStore.data.map { it[dailyVerseVisibleKey] ?: true }
    val quranModes: Flow<Set<String>> =
        context.dataStore.data.map {
            it[quranModesKey] ?: QuranModes.defaults(QuranModes.currentLanguageTag())
        }
    val themeMode: Flow<String> =
        context.dataStore.data.map { it[themeKey] ?: "dark" }
    val prayerOffsets: Flow<Map<Prayer, Int>> =
        context.dataStore.data.map { preferences ->
            PrayerOffsets.decode(preferences[prayerOffsetsKey])
        }
    val hijriOffset: Flow<Int> =
        context.dataStore.data.map { preferences ->
            HijriOffset.sanitize(preferences[hijriOffsetKey] ?: HijriOffset.DEFAULT)
        }

    /** Minutos do lembrete antes do adhan; 0 desliga. */
    val adhanReminderMinutes: Flow<Int> =
        context.dataStore.data.map { preferences ->
            PrayerReminder.sanitize(preferences[adhanReminderKey])
        }

    /** Exceções por oração; ausência da oração = herda o global. */
    val adhanSoundsByPrayer: Flow<Map<Prayer, AdhanSound>> =
        context.dataStore.data.map { AdhanOverrides.decodeSounds(it[adhanSoundByPrayerKey]) }
    val adhanRemindersByPrayer: Flow<Map<Prayer, Int>> =
        context.dataStore.data.map { AdhanOverrides.decodeReminders(it[adhanReminderByPrayerKey]) }
    val adhanAlarmByPrayer: Flow<Set<Prayer>> =
        context.dataStore.data.map { AdhanOverrides.decodeAlarm(it[adhanAlarmByPrayerKey]) }
    val adhanRepeatsByPrayer: Flow<Map<Prayer, Set<DayOfWeek>>> =
        context.dataStore.data.map { AdhanOverrides.decodeRepeats(it[adhanRepeatsByPrayerKey]) }

    /** `null` = wizard do Alcorão ainda não foi visto nem pulado. */
    val quranSetupDone: Flow<Boolean?> =
        context.dataStore.data.map { it[quranSetupDoneKey] }

    val quranGoalMinutes: Flow<Int> =
        context.dataStore.data.map { QuranGoal.sanitize(it[quranGoalMinutesKey]) }

    val readerTheme: Flow<ReaderTheme> =
        context.dataStore.data.map { ReaderTheme.fromId(it[readerThemeKey]) }

    val readerFont: Flow<ReaderFont> =
        context.dataStore.data.map { ReaderFont.fromId(it[readerFontKey]) }

    val readerMode: Flow<ReaderMode> =
        context.dataStore.data.map { ReaderMode.fromId(it[readerModeKey]) }

    val readerKeepScreenOn: Flow<Boolean> =
        context.dataStore.data.map { it[readerKeepScreenOnKey] ?: false }

    val quranSpeed: Flow<Float> =
        context.dataStore.data.map { preferences ->
            ReaderPrefs.speed(preferences[quranSpeedKey]?.toFloatOrNull())
        }

    /** `null` = primeira execucao (onboarding ainda nao foi concluido ou pulado). */
    val onboardingDone: Flow<Boolean?> =
        context.dataStore.data.map { it[onboardingDoneKey] }

    override fun observePromptDismissed(): Flow<Boolean> =
        context.dataStore.data.map { it[updatePromptDismissedKey] ?: false }

    override suspend fun setPromptDismissed(value: Boolean) {
        context.dataStore.edit { it[updatePromptDismissedKey] = value }
    }

    suspend fun setQuranReciterId(id: String) { context.dataStore.edit { it[quranReciterKey] = id } }

    suspend fun setCity(id: String) {
        context.dataStore.edit { it[cityKey] = id }
    }

    suspend fun setMethod(name: String) {
        context.dataStore.edit { it[methodKey] = name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[notificationsKey] = enabled }
    }

    suspend fun setAdhanPrayers(prayers: Set<Prayer>) {
        context.dataStore.edit { it[adhanPrayersKey] = AdhanPolicy.encode(prayers) }
    }

    suspend fun setAdhanSound(id: String) {
        context.dataStore.edit { it[adhanSoundKey] = AdhanSound.findNormal(id).id }
    }

    suspend fun setAdhanFajrSound(id: String) {
        context.dataStore.edit { it[adhanFajrSoundKey] = AdhanSound.findFajr(id).id }
    }

    suspend fun setAdhanFajrOverride(value: Boolean) {
        context.dataStore.edit { it[adhanFajrOverrideKey] = value }
    }

    suspend fun dismissAdhanFajrNotice() {
        context.dataStore.edit { it[adhanFajrNoticeKey] = false }
    }

    /**
     * Ate a 1.2.0 as duas gravacoes de Madinah com tathwib podiam ser
     * escolhidas como voz global. Na primeira abertura depois da atualizacao,
     * elas passam para o Fajr e o dia volta ao classico, com um aviso unico.
     */
    suspend fun migrateAdhan() {
        context.dataStore.edit { preferences ->
            val plan = AdhanMigrationPlan.plan(
                globalId = preferences[adhanSoundKey],
                fajrId = preferences[adhanFajrSoundKey],
            )
            plan.globalId?.let { preferences[adhanSoundKey] = it }
            plan.fajrId?.let { preferences[adhanFajrSoundKey] = it }
            plan.override?.let { preferences[adhanFajrOverrideKey] = it }
            if (plan.showNotice) {
                preferences[adhanFajrNoticeKey] = true
            }
        }
    }

    suspend fun setAdhanRespectSilent(value: Boolean) {
        context.dataStore.edit { it[adhanRespectSilentKey] = value }
    }

    suspend fun setAdhanReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[adhanReminderKey] = PrayerReminder.sanitize(minutes) }
    }

    suspend fun setAdhanSoundsByPrayer(overrides: Map<Prayer, AdhanSound>) {
        context.dataStore.edit {
            it[adhanSoundByPrayerKey] = AdhanOverrides.encodeSounds(overrides)
        }
    }

    suspend fun setAdhanRemindersByPrayer(overrides: Map<Prayer, Int>) {
        context.dataStore.edit {
            it[adhanReminderByPrayerKey] = AdhanOverrides.encodeReminders(overrides)
        }
    }

    suspend fun setAdhanAlarmByPrayer(prayers: Set<Prayer>) {
        context.dataStore.edit {
            it[adhanAlarmByPrayerKey] = AdhanOverrides.encodeAlarm(prayers)
        }
    }

    suspend fun setAdhanRepeatsByPrayer(overrides: Map<Prayer, Set<DayOfWeek>>) {
        context.dataStore.edit {
            it[adhanRepeatsByPrayerKey] = AdhanOverrides.encodeRepeats(overrides)
        }
    }

    /** Grava a conclusão tanto no "Começar" quanto no "Pular" do wizard. */
    suspend fun setQuranSetupDone(done: Boolean = true) {
        context.dataStore.edit { it[quranSetupDoneKey] = done }
    }

    suspend fun setQuranGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[quranGoalMinutesKey] = QuranGoal.sanitize(minutes) }
    }

    suspend fun setReaderTheme(theme: ReaderTheme) {
        context.dataStore.edit { it[readerThemeKey] = theme.id }
    }

    suspend fun setReaderFont(font: ReaderFont) {
        context.dataStore.edit { it[readerFontKey] = font.id }
    }

    suspend fun setReaderMode(mode: ReaderMode) {
        context.dataStore.edit { it[readerModeKey] = mode.id }
    }

    suspend fun setReaderKeepScreenOn(value: Boolean) {
        context.dataStore.edit { it[readerKeepScreenOnKey] = value }
    }

    suspend fun setQuranSpeed(speed: Float) {
        context.dataStore.edit { it[quranSpeedKey] = ReaderPrefs.speed(speed).toString() }
    }

    suspend fun setDailyVerseVisible(value: Boolean) {
        context.dataStore.edit { it[dailyVerseVisibleKey] = value }
    }

    suspend fun setQuranModes(modes: Set<String>) {
        context.dataStore.edit { it[quranModesKey] = modes }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[themeKey] = mode }
    }

    /** Aceita apenas -1, 0 e +1; valores fora da faixa viram o padrao. */
    suspend fun setHijriOffset(days: Int) {
        context.dataStore.edit { it[hijriOffsetKey] = HijriOffset.sanitize(days) }
    }

    /** Grava a flag tanto no "Comecar" quanto no "Pular". */
    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[onboardingDoneKey] = true }
    }

    suspend fun setPrayerOffset(prayer: Prayer, minutes: Int) {
        updateOffset(prayer) { PrayerOffsets.clamp(minutes) }
    }

    /**
     * Soma [delta] ao valor salvo dentro do mesmo `edit`, para que toques
     * seguidos no dialogo nao percam incremento por estado atrasado da UI.
     */
    suspend fun nudgePrayerOffset(prayer: Prayer, delta: Int) {
        updateOffset(prayer) { previous -> PrayerOffsets.clamp(previous + delta) }
    }

    private suspend fun updateOffset(prayer: Prayer, value: (Int) -> Int) {
        context.dataStore.edit { preferences ->
            val current = PrayerOffsets.decode(preferences[prayerOffsetsKey])
            val updated = current.toMutableMap()
            updated[prayer] = value(current[prayer] ?: 0)
            preferences[prayerOffsetsKey] = PrayerOffsets.encode(updated)
        }
    }

    private fun defaultCityId(): String =
        cities.defaultForZone(ZoneId.systemDefault().id)?.id ?: cities.first().id
}
