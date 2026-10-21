package com.farukzahra.muslimplus

import android.content.res.Configuration
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.farukzahra.muslimplus.settings.LanguageRepository
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Testes de fumaca no aparelho: navegacao entre as cinco abas, troca de idioma
 * (ida e volta), leitor do Alcorao e a grade do Mais.
 *
 * O app abre no onboarding na primeira execucao. A regra externa marca a flag
 * antes da activity subir, para que todos os testes comecem na tela inicial e
 * nao dependam de um teste anterior ter pulado o onboarding.
 *
 * Nada e comparado com string literal em portugues: os rotulos saem de `R.string`
 * no locale corrente (ou no locale forcado, via [stringIn]), entao o teste passa
 * com o aparelho em pt-BR, en ou ar.
 */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    /** Roda antes da activity: onboarding concluido e idioma do sistema. */
    private val prepare = object : ExternalResource() {
        override fun before() {
            // Qualquer teste pode ter deixado o app em arabe (inclusive ao
            // falhar no meio). Volta ao padrao do sistema e espera aplicar,
            // porque setApplicationLocales e assincrono e sozinho nao garante
            // que a activity ja suba no locale do aparelho.
            LanguageRepository.set(null)
            val deadline = System.currentTimeMillis() + RESET_MS
            while (LanguageRepository.currentTag() != null && System.currentTimeMillis() < deadline) {
                Thread.sleep(50)
            }
            runBlocking { ServiceLocator.settings.setOnboardingDone() }
        }
    }

    private val compose = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(prepare).around(compose)

    @Test
    fun cincoAbasNavegamSemCrash() {
        val ctx = compose.activity
        val tabs = listOf(
            ctx.getString(R.string.tab_today),
            ctx.getString(R.string.tab_prayers),
            ctx.getString(R.string.tab_quran),
            ctx.getString(R.string.tab_qibla),
            ctx.getString(R.string.tab_more),
        )
        // A barra inferior mostra as cinco abas na abertura.
        tabs.forEach { compose.onNodeWithText(it).assertExists() }

        // Hoje: a lista de oracoes carrega depois do estado do ViewModel.
        awaitText(ctx.getString(R.string.prayer_fajr))

        compose.onNodeWithText(ctx.getString(R.string.tab_prayers)).performClick()
        awaitText(ctx.getString(R.string.prayers_adjust))

        compose.onNodeWithText(ctx.getString(R.string.tab_quran)).performClick()
        awaitText(AL_FATIHAH)

        compose.onNodeWithText(ctx.getString(R.string.tab_qibla)).performClick()
        awaitAny(
            ctx.getString(R.string.qibla_calibration),
            ctx.getString(R.string.qibla_no_sensor),
        )

        compose.onNodeWithText(ctx.getString(R.string.tab_more)).performClick()
        awaitText(ctx.getString(R.string.more_settings))

        // Volta para Hoje: a aba continua funcional depois de passar por todas.
        compose.onNodeWithText(ctx.getString(R.string.tab_today)).performClick()
        awaitText(ctx.getString(R.string.prayer_fajr))
    }

    @Test
    fun trocaParaArabeEVoltaAoSistema() {
        val arabic = { id: Int -> stringIn("ar", id) }
        // Sempre le o contexto da activity atual: cada troca de idioma recria a
        // activity e o contexto antigo fica preso ao locale anterior.
        val device = { id: Int -> compose.activity.getString(id) }

        openLanguageScreen()

        compose.onNodeWithText(arabic(R.string.language_arabic)).performClick()
        // AppCompat recria a activity; o titulo em arabe confirma o locale novo.
        awaitText(arabic(R.string.language_title))

        // Volta para a grade do Mais: as abas tambem estao em arabe.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitText(arabic(R.string.tab_today))
        compose.onNodeWithText(arabic(R.string.tab_prayers)).assertExists()
        // "المزيد" aparece duas vezes (aba + titulo da tela); basta existir.
        compose.onAllNodes(hasText(arabic(R.string.tab_more))).onFirst().assertExists()

        // Troca de volta para "padrao do sistema" pela mesma tela; o back acima
        // ja deixou a grade do Mais na frente.
        compose.onNodeWithText(arabic(R.string.more_language)).performClick()
        awaitText(arabic(R.string.language_title))
        compose.onNodeWithText(arabic(R.string.language_system)).performClick()

        // De volta ao idioma do aparelho (a activity foi recriada de novo).
        compose.waitUntil(TIMEOUT_MS) { LanguageRepository.currentTag() == null }
        awaitText(device(R.string.language_title))
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitText(device(R.string.tab_today))
    }

    @Test
    fun abreAlFatihahEMostraPrimeiroVerso() {
        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.tab_quran)).performClick()
        awaitText(AL_FATIHAH)
        compose.onNodeWithText(AL_FATIHAH).performClick()

        // O texto esperado vem do mesmo asset que o leitor usa, sem duplicar
        // harakat na mao.
        val firstVerse = requireNotNull(ServiceLocator.quran.verse(1, 1))
        awaitText(firstVerse.text)
        // A referencia "1:1" sai formatada no locale corrente: em arabe os
        // digitos viram arabico-indicos, entao o esperado vem do recurso.
        awaitText(ctx.getString(R.string.verse_reference, 1, 1))
    }

    @Test
    fun dialogoDeCamadasDoLeitorAbre() {
        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.tab_quran)).performClick()
        awaitText(AL_FATIHAH)
        compose.onNodeWithText(AL_FATIHAH).performClick()
        awaitText(requireNotNull(ServiceLocator.quran.verse(1, 1)).text)

        compose.onNodeWithContentDescription(ctx.getString(R.string.quran_display)).performClick()
        awaitText(ctx.getString(R.string.quran_arabic))
        compose.onNodeWithText(ctx.getString(R.string.action_ok)).performClick()
        compose.onNodeWithText(ctx.getString(R.string.quran_keep_one)).assertDoesNotExist()
    }

    @Test
    fun gradeDoMaisAbreConfiguracoes() {
        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.tab_more)).performClick()
        awaitText(ctx.getString(R.string.more_settings))

        // A grade tem seis destinos; aqui basta o primeiro.
        compose.onNodeWithText(ctx.getString(R.string.more_settings)).performClick()
        awaitText(ctx.getString(R.string.prayers_city))
        compose.onNodeWithText(ctx.getString(R.string.prayers_method)).assertExists()
        compose.onNodeWithText(ctx.getString(R.string.settings_notifications_hint)).assertExists()
    }

    private fun openLanguageScreen() {
        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.tab_more)).performClick()
        awaitText(ctx.getString(R.string.more_language))
        compose.onNodeWithText(ctx.getString(R.string.more_language)).performClick()
        awaitText(ctx.getString(R.string.language_title))
    }

    /** Rotulo no locale pedido, independente do idioma do aparelho. */
    private fun stringIn(tag: String, id: Int): String {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(tag))
        }
        return base.createConfigurationContext(configuration).getString(id)
    }

    private fun awaitText(text: String, substring: Boolean = false): SemanticsNodeInteraction {
        compose.waitUntil(TIMEOUT_MS) {
            compose.onAllNodes(hasText(text, substring = substring))
                .fetchSemanticsNodes().isNotEmpty()
        }
        return compose.onAllNodes(hasText(text, substring = substring)).onFirst()
    }

    private fun awaitAny(vararg texts: String) {
        compose.waitUntil(TIMEOUT_MS) {
            texts.any { text ->
                compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }

    private companion object {
        /** Nome latino da primeira sura, igual nos tres idiomas do catalogo. */
        const val AL_FATIHAH = "Al-Fatihah"

        /** Leitura de assets + recriacao de activity em emulador pode demorar. */
        const val TIMEOUT_MS = 20_000L

        /** Tempo maximo para o locale voltar ao padrao antes da activity subir. */
        const val RESET_MS = 5_000L
    }
}
