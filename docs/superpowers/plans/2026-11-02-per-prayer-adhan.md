# Plano — adhan por oração (som, lembrete, alarme e repetição)

Data: 02/11/2026, revisado em 03/11/2026. Versão alvo: 1.4.0 (versionCode 10).
Spec: [`specs/2026-11-02-per-prayer-adhan.md`](../specs/2026-11-02-per-prayer-adhan.md).
Pesquisa: [`docs/research/2026-11-02-muslim-pro-paid-features.md`](../../research/2026-11-02-muslim-pro-paid-features.md).

## Tarefas

| # | Tarefa | Estado |
| --- | --- | --- |
| 1 | `AdhanOverrides` puro (som, lembrete, alarme, repetição) + `AdhanOverridesTest` | Feito |
| 2 | `AdhanPolicy.resolveFor` e `shouldPlay` com alarme e dia; `PrayerReminder` por oração + testes | Feito |
| 3 | Quatro chaves novas no `SettingsRepository` | Feito |
| 4 | `PrayerScheduler` com minutos por oração e `EXTRA_REMINDER_MINUTES`; `PrayerReceiver` com alarme e repetição | Feito |
| 5 | `PrayerAdhanSheet`: rascunho, Notificar, Alarme, Repetir, Som, Lembrete, prévia, Salvar | Feito |
| 6 | Lista de som dentro da sheet, com “usar padrão”, filtro Fajr e prévia | Feito |
| 7 | Sino da linha do Hoje abre a sheet | Feito |
| 8 | Nota e atalho na tela do adhan global | Feito |
| 9 | Strings nos três idiomas | Feito |
| 10 | E2E `PrayerAdhanSheetTest` (3 casos, 6 cenários) | Feito |
| 11 | `test`, `lint`, `connectedDebugAndroidTest`, `install-local` | Feito |
| 12 | Versão 1.4.0, notas em pt-BR/en/ar, AAB e publicação | Notas prontas; publicação aguarda autorização |
| 13 | Arte e legenda do comparativo no Instagram (depois de 1.4.0) | Pendente |

## Ordem de execução

1. **Tarefas 1 e 2 (TDD puro).** Escrever `AdhanOverridesTest` e os casos novos
   de `AdhanPolicyTest`/`PrayerRemindersTest` antes do código. Rodar
   `gradlew test` e ver falhar.
2. **Tarefas 3 e 4 (dados e alarme).** `SettingsRepository` expõe os quatro
   mapas sanitizados e os setters; agendamento e receiver passam a usar minutos,
   alarme e dias por oração.
3. **Tarefas 5 a 7 (UI).** Sheet em `ui/screens/today/PrayerAdhanSheet.kt`,
   reaproveitando a lista de vozes da `AdhanScreen` extraída para um composable
   comum. `TodayScreen` troca o `onClick` do sino.
4. **Tarefa 8 (tela global).** Nota curta e atalho “Ver por oração”.
5. **Tarefa 9.** Strings depois da UI fechada.
6. **Tarefas 10 e 11.** E2E nos dois emuladores, `install-local` com `OK`.
7. **Tarefa 12.** Só com autorização explícita de publicação.
8. **Tarefa 13.** Só depois do 1.4.0 instalado.

## Riscos e cuidados

- **Duas fontes de verdade.** O global (`adhan_sound`) e o override por oração
  não podem divergir: toda leitura de voz efetiva passa por
  `AdhanPolicy.resolveFor`.
- **Voz de Fajr.** `decodeSounds` valida por oração; override inválido é
  descartado na leitura, não reescrito na escrita.
- **Lembrete único.** Mudar minutos de qualquer oração exige `reschedule()`
  imediato, senão o aviso antigo continua.
- **Texto do lembrete.** O receiver não recalcula minutos; usa o extra do
  intent.
- **Repetição.** Dia não marcado não pode cancelar o alarme nem parar de
  reagendar, senão o app fica mudo até abrir de novo.
- **Alarme completo.** Não usar `USE_FULL_SCREEN_INTENT`; é áudio no volume de
  alarme, não tela cheia.
- **Fechar sem salvar.** Descartar o rascunho ao fechar, ao trocar de oração e
  ao girar a tela.
- **Sem asset do concorrente.** Nomes de vozes continuam os nossos.

## Evidências a registrar no fim

- `gradlew test` e `lint`: verdes, com a contagem de testes.
- `connectedDebugAndroidTest`: quantos/quantos em `MuslimPlusP0` e
  `Pixel6Test`.
- `install-local.ps1`: `OK <serial>` nos dois emuladores, com o hash novo.
- Print da sheet do Fajr com som próprio, alarme e dias, e do Maghrib herdando.
- `docs/releases/1.4.0.md` com notas nos três idiomas e mensagem de WhatsApp.
