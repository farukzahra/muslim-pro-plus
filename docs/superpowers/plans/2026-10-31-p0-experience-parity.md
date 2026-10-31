# Plano — P0 de experiência (31/10/2026)

Spec: `docs/superpowers/specs/2026-10-31-p0-experience-parity.md`

Cada tarefa termina com teste verde e commit próprio. Uma tarefa só começa
depois da anterior estar verde, salvo quando o plano disser o contrário.

## T1 — Hoje acionável

- [x] T1.1 `prayer/PrayerLog.kt`: conjunto rastreável, codec por data,
  progresso, sequência e faixa semanal (funções puras).
- [x] T1.2 `prayer/PrayerLogRepository.kt`: DataStore `prayer_log`, toggle,
  marcar todas, poda de 60 dias; registrar no `ServiceLocator`.
- [x] T1.3 `TodayViewModel`: expor dia atual, semana, sequência e ações.
- [x] T1.4 `TodayScreen`: anel de progresso, faixa de sete dias, check por
  oração, marcar todas e sino por oração.
- [x] T1.5 Strings nos três locales e testes JVM (`PrayerLogTest`).
- [x] T1.6 Teste instrumentado `TodayTrackerTest` (check, marcar todas, sino).

## T2 — Setup do Alcorão

- [x] T2.1 `settings/QuranGoal.kt`: metas permitidas, sanitização e estimativa
  de conclusão em dias.
- [x] T2.2 `SettingsRepository`: `quranSetupDone`, `quranGoalMinutes` e setters.
- [x] T2.3 `ui/screens/quran/QuranSetupScreen.kt`: três passos com pular.
- [x] T2.4 `QuranListScreen`: exibe o wizard enquanto não concluído; abas
  Leitura e Progresso.
- [x] T2.5 `QuranViewModel`: estado do wizard, meta, minutos lidos e progresso.
- [x] T2.6 Teste JVM `QuranGoalTest` e instrumentado `QuranSetupTest`.

## T3 — Experiência do leitor

- [x] T3.1 `settings/ReaderPrefs.kt`: temas, escala de fonte, modo de leitura e
  velocidade válidos; persistência em `SettingsRepository`.
- [x] T3.2 `quran/audio/QuranAudioController`: `next`, `previous`, `setSpeed` e
  velocidade no estado.
- [x] T3.3 `QuranReaderScreen`: paleta por tema, escala de fonte, bottom sheet,
  manter tela ligada, chip de referência e mini-player.
- [x] T3.4 Modo Página com pager de um verso e sincronização da última leitura.
- [x] T3.5 Teste JVM `ReaderPrefsTest` e instrumentado `QuranReaderSettingsTest`.

## T4 — Adhan por oração e lembrete

- [x] T4.1 `prayer/PrayerReminders.kt`: opções, sanitização e próximo aviso.
- [x] T4.2 `SettingsRepository`: `adhanReminderMinutes` e setter.
- [x] T4.3 `PrayerScheduler`: segundo alarme de lembrete com request code
  próprio; `PrayerReceiver` mostra notificação sem tocar áudio.
- [x] T4.4 `AdhanScreen`: escolha do lembrete antes do adhan.
- [x] T4.5 Teste JVM `PrayerRemindersTest` e instrumentado na tela de adhan.

## T5 — Qibla confiável

- [x] T5.1 `qibla/QiblaCompass.kt`: mapa de precisão, interferência, tolerância
  de 5° e texto de estado (funções puras).
- [x] T5.2 `QiblaViewModel`: precisão do sensor no estado, amostra com rumo.
- [x] T5.3 `QiblaScreen`: aviso de interferência, "de frente para Makkah" e
  diálogo de ajuda de calibração.
- [x] T5.4 Teste JVM `QiblaCompassTest` e instrumentado `QiblaHelpTest`.

## T6 — Polimento visual e onboarding

- [x] T6.1 Formas no tema (cartões 20 dp) e componentes compartilhados
  (`InfoCard`, `Pill`).
- [x] T6.2 Onboarding com cartões de permissão e bloco de privacidade.
- [x] T6.3 Ajustes de espaçamento no Hoje, Alcorão e Mais sem mudar navegação.
- [x] T6.4 Teste instrumentado do onboarding (cartões e início).

## Fechamento

- [x] Rodar `gradlew test lint assembleDebug`.
- [x] Rodar `connectedDebugAndroidTest` no emulador.
- [x] `scripts/install-local.ps1` com `OK <serial>`.
- [x] Screenshot das telas novas e pedido de validação ao usuário.
- [x] Commit final com versão semântica.
