package com.farukzahra.muslimplus.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.settings.QuranGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Wizard de três passos da primeira entrada no Alcorão: escrita, tradução e
 * meta diária. "Pular" grava a conclusão e mantém os padrões atuais. Cada
 * escolha é persistida na hora, então sair no meio não perde nada.
 */
@Composable
fun QuranSetupScreen(onDone: () -> Unit) {
    val settings = ServiceLocator.settings
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    val modes by settings.quranModes.collectAsState(initial = setOf("ar"))
    val goal by settings.quranGoalMinutes.collectAsState(initial = QuranGoal.DEFAULT_MINUTES)

    val steps = 3

    fun finish() {
        scope.launch {
            settings.setQuranSetupDone()
            onDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("quran_setup"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.quran_setup_step, step + 1, steps),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { finish() },
                modifier = Modifier.testTag("quran_setup_skip"),
            ) {
                Text(stringResource(R.string.quran_setup_skip))
            }
        }
        Text(
            text = when (step) {
                0 -> stringResource(R.string.quran_setup_script_title)
                1 -> stringResource(R.string.quran_setup_translation_title)
                else -> stringResource(R.string.quran_setup_goal_title)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = when (step) {
                0 -> stringResource(R.string.quran_setup_script_body)
                1 -> stringResource(R.string.quran_setup_translation_body)
                else -> stringResource(R.string.quran_setup_goal_body)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(Modifier.height(20.dp))
        ArabicPreview()
        Spacer(Modifier.height(20.dp))

        when (step) {
            0 -> ScriptInfoCard()
            1 -> TranslationChoices(
                modes = modes,
                onPick = { selected -> scope.launch { settings.setQuranModes(selected) } },
            )
            else -> GoalChoices(
                goal = goal,
                onPick = { minutes -> scope.launch { settings.setQuranGoalMinutes(minutes) } },
            )
        }

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                TextButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("quran_setup_back"),
                ) {
                    Text(stringResource(R.string.action_back))
                }
            }
            Button(
                onClick = {
                    if (step < steps - 1) step++ else finish()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag(if (step < steps - 1) "quran_setup_next" else "quran_setup_start"),
            ) {
                Text(
                    stringResource(
                        if (step < steps - 1) R.string.quran_setup_next else R.string.quran_setup_start,
                    ),
                )
            }
        }
    }
}

/** Prévia do texto árabe vinda do mesmo asset do leitor, sem hardcode. */
@Composable
private fun ArabicPreview() {
    val verse by produceState<String?>(null) {
        value = withContext(Dispatchers.IO) { ServiceLocator.quran.verse(1, 1)?.text }
    }
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("quran_setup_preview"),
    ) {
        Text(
            text = stringResource(R.string.quran_setup_preview),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.secondary,
        )
        Text(
            text = verse.orEmpty(),
            fontSize = 24.sp,
            lineHeight = 42.sp,
            textAlign = TextAlign.Start,
            style = TextStyle(color = scheme.onSurface, textDirection = TextDirection.Rtl),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ScriptInfoCard() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, scheme.primary, RoundedCornerShape(20.dp))
            .background(scheme.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = scheme.primary,
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = stringResource(R.string.quran_script_uthmani),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
            Text(
                text = stringResource(R.string.quran_setup_script_note),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private data class TranslationChoice(val labelRes: Int, val modes: Set<String>)

private val translationChoices = listOf(
    TranslationChoice(R.string.quran_setup_only_arabic, setOf("ar")),
    TranslationChoice(R.string.language_portuguese, setOf("ar", "pt-BR")),
    TranslationChoice(R.string.language_english, setOf("ar", "en")),
)

@Composable
private fun TranslationChoices(modes: Set<String>, onPick: (Set<String>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        translationChoices.forEach { choice ->
            val selected = choice.modes == modes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        RoundedCornerShape(16.dp),
                    )
                    .selectable(selected = selected, role = Role.RadioButton) { onPick(choice.modes) }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .testTag(
                        when (choice.modes) {
                            setOf("ar") -> "quran_setup_translation_ar"
                            setOf("ar", "pt-BR") -> "quran_setup_translation_pt"
                            else -> "quran_setup_translation_en"
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = stringResource(choice.labelRes),
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun GoalChoices(goal: Int, onPick: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuranGoal.options.forEach { minutes ->
            val selected = minutes == goal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) scheme.primary.copy(alpha = 0.10f) else scheme.surfaceContainer,
                        RoundedCornerShape(16.dp),
                    )
                    .selectable(selected = selected, role = Role.RadioButton) { onPick(minutes) }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .testTag("quran_setup_goal_$minutes"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = stringResource(R.string.quran_setup_goal_option, minutes),
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.quran_setup_khatam, QuranGoal.khatamDays(goal)),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.primary,
            modifier = Modifier.testTag("quran_setup_khatam"),
        )
    }
}
