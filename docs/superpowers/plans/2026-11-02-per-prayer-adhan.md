# Plano — adhan por oração (som e lembrete)

Data: 02/11/2026. Versão alvo: 1.4.0 (versionCode 10).
Spec: [`specs/2026-11-02-per-prayer-adhan.md`](../specs/2026-11-02-per-prayer-adhan.md).
Pesquisa: [`docs/research/2026-11-02-muslim-pro-paid-features.md`](../../research/2026-11-02-muslim-pro-paid-features.md).

## Tarefas

| # | Tarefa | Estado |
| --- | --- | --- |
| 1 | `AdhanOverrides` puro (encode/decode de som e lembrete) + `AdhanOverridesTest` | Pendente |
| 2 | `AdhanPolicy.resolveFor` e `PrayerReminder.minutesFor`/`next` por oração + testes | Pendente |
| 3 | Chaves `adhan_sound_by_prayer` e `adhan_reminder_by_prayer` no `SettingsRepository` | Pendente |
| 4 | `PrayerScheduler` com minutos por oração e `EXTRA_REMINDER_MINUTES`; `PrayerReceiver` lendo o intent | Pendente |
| 5 | `PrayerAdhanSheet`: rascunho, switch Notify, som, lembrete, prévia, Salvar | Pendente |
| 6 | Lista de som dentro da sheet, com “usar padrão”, filtro Fajr e prévia | Pendente |
| 7 | Sino da linha do Hoje abre a sheet em vez de alternar | Pendente |
| 8 | Nota e atalho na tela do adhan global | Pendente |
| 9 | Strings nos três idiomas | Pendente |
| 10 | E2E `PrayerAdhanSheetTest` (5 cenários) | Pendente |
| 11 | `test`, `lint`, `connectedDebugAndroidTest`, `install-local` | Pendente |
| 12 | Versão 1.4.0, notas em pt-BR/en/ar, AAB e publicação | Pendente |
| 13 | Arte e legenda do comparativo no Instagram (depois de 1.4.0) | Pendente |

## Ordem de execução

1. **Tarefas 1 a 2 (TDD puro).** Escrever `AdhanOverridesTest` e os casos novos
   de `AdhanPolicyTest`/`PrayerRemindersTest` antes do código. Rodar
   `gradlew test` e ver falhar.
2. **Tarefas 3 a 4 (dados e alarme).** `SettingsRepository` expõe os mapas
   sanitizados e os setters; agendamento e receiver passam a usar minutos por
   oração. Teste de intenção cobre o extra novo.
3. **Tarefas 5 a 7 (UI).** Sheet nova em `ui/screens/today/PrayerAdhanSheet.kt`,
   reaproveitando a lista de vozes da `AdhanScreen` extraída para um composable
   comum. `TodayScreen` só troca o `onClick` do sino e passa os estados.
4. **Tarefa 8 (tela global).** Nota curta e atalho, sem mudar o comportamento
   dos cartões padrão/Fajr.
5. **Tarefa 9.** Só depois da UI fechada, para não repetir string.
6. **Tarefas 10 a 11.** E2E nos dois emuladores, `install-local` com `OK`.
7. **Tarefa 12.** Só com autorização explícita de publicação.
8. **Tarefa 13.** Só depois do 1.4.0 estar instalado, para o comparativo não
   prometer recurso que não existe.

## Riscos e cuidados

- **Duas fontes de verdade.** O global (`adhan_sound`) e o override por oração
  não podem divergir: qualquer leitura de voz efetiva passa por
  `AdhanPolicy.resolveFor`.
- **Voz de Fajr.** `decodeSounds` valida por oração; um override inválido é
  descartado na leitura, não “corrigido” na escrita, para não reescrever a
  preferência do usuário silenciosamente.
- **Lembrete único.** Como só existe um alarme de lembrete agendado, mudar
  minutos de uma oração exige `reschedule()` imediato, senão o aviso antigo
  continua.
- **Texto do lembrete.** O receiver não pode recalcular minutos; usa o extra do
  intent.
- **Fechar sem salvar.** O rascunho precisa ser descartado também ao trocar de
  oração e ao girar a tela.
- **Sem asset do concorrente.** Nomes de vozes continuam os nossos.

## Evidências a registrar no fim

- `gradlew test` e `lint`: verdes, com a contagem de testes.
- `connectedDebugAndroidTest`: quantos/quantos em `MuslimPlusP0` e
  `Pixel6Test`.
- `install-local.ps1`: `OK <serial>` nos dois emuladores, com o hash novo.
- Print da sheet do Fajr com som próprio e do Maghrib com padrão.
- `docs/releases/1.4.0.md` com notas nos três idiomas e mensagem de WhatsApp.
