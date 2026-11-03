# Adhan por oração — som, lembrete, alarme e repetição

Data: 02/11/2026, revisado em 03/11/2026. Status: **aprovado em 03/11/2026,
com a fase 2 incluída, e implementado na 1.4.0**.
Pesquisa: [`docs/research/2026-11-02-muslim-pro-paid-features.md`](../../research/2026-11-02-muslim-pro-paid-features.md).
Substitui a decisão “cinco seletores por oração ficam para a segunda etapa” do
spec [`2026-11-01-adhan-fajr-e-catalogo.md`](2026-11-01-adhan-fajr-e-catalogo.md).

## Por que

O Muslim Pro deixa escolher, **por oração**, o som do adhan, o lembrete antes
dele, se toca como alarme completo e em quais dias da semana repete. Hoje o
Muslim Plus tem só duas coisas: liga/desliga por oração (`adhanPrayers`) e uma
voz global com exceção do Fajr. Quem quer o adhan da Makkah no Maghrib e o de
Madinah no Fajr não consegue.

O pedido do usuário foi “pegar as funcionalidades pagas do Muslim Pro e
implementar aqui”. Em 03/11/2026 o usuário aprovou incluir a fase 2 agora:
alarme completo e repetição por dia entram nesta versão.

## Decisões

1. **Exceção por oração sobre o padrão**, não cinco configurações
   independentes. Quem não mexer em nada continua com o comportamento de hoje.
2. **Quatro controles independentes**: notificar, som, lembrete, alarme
   completo. Repetição de dias é o quinto.
3. **A sheet substitui o toggle direto** no ícone de sino da linha do Hoje. O
   sino continua mostrando ligado/desligado.
4. **Salvar é explícito**. Fechar a sheet descarta, como no concorrente.
5. **Sem conta, sem download, sem áudio novo**: as 22 vozes embarcadas
   continuam sendo o catálogo.
6. **Sem tela cheia / `USE_FULL_SCREEN_INTENT`**. “Alarme completo” aqui é
   áudio: toca no volume de alarme ignorando o modo silencioso, como já fazemos
   por padrão; o que muda é poder **desligar** isso por oração.
7. **Repetição não silencia o agendamento.** O alarme ainda dispara e reagenda;
   num dia não repetido ele não mostra notificação nem toca o adhan.

## Modelo de dados

Quatro chaves novas no DataStore. Nenhuma migração: ausência = herda o global.

| Chave | Formato | Exemplo |
| --- | --- | --- |
| `adhan_sound_by_prayer` | `ORAÇÃO=id;…` | `FAJR=madinah-nafman;ISHA=makkah-mulla` |
| `adhan_reminder_by_prayer` | `ORAÇÃO=minutos;…` | `FAJR=15;ISHA=0` |
| `adhan_alarm_by_prayer` | `ORAÇÃO;…` | `FAJR;MAGHRIB` |
| `adhan_repeats_by_prayer` | `ORAÇÃO=1,2,…;…` | `FAJR=1,2,3,4,5,6,7;ISHA=5` |

Regras:

- `0` no lembrete significa “desligado só nesta oração”.
- Oração ausente no lembrete herda o global; ausente no alarme herda
  `adhan_respect_silent` (o interruptor global de hoje).
- Dias são `java.time.DayOfWeek.value` (1 = segunda, 7 = domingo).
- Conjunto de dias vazio é lido como “todos os dias”, para um dia desselecionado
  por engano não silenciar a oração para sempre.

## Regras puras

Novo objeto `adhan/AdhanOverrides.kt`, sem Android:

- `decodeSounds(raw: String?): Map<Prayer, String>` — ignora `SUNRISE`, id
  desconhecido e voz de Fajr em oração comum (e o contrário).
- `encodeSounds(map): String` — ordem fixa de `Prayer.entries`, separador `;`.
- `decodeReminders(raw: String?): Map<Prayer, Int>` — descarta valor fora de
  `PrayerReminder.options`.
- `encodeReminders(map): String`.
- `decodeAlarm(raw: String?): Set<Prayer>` — só as cinco orações.
- `encodeAlarm(set): String`.
- `decodeRepeats(raw: String?): Map<Prayer, Set<DayOfWeek>>` — descarta dias
  fora de 1..7 e trata vazio como todos.
- `encodeRepeats(map): String` — só grava quem não está em “todos”, para a
  preferência ficar curta.

Em `AdhanPolicy`:

- `resolveFor(prayer, global, fajr, fajrOverride, overrides): AdhanSound` —
  override da oração quando existe e é válido; senão cai em `resolve(...)`.
- `shouldPlay(prayer, enabled, respectSilent, ringer, alarm: Boolean,
  repeats: Set<DayOfWeek>?, day: DayOfWeek)` — respeita o alarme por oração
  (alarme ligado ignora `respectSilent`) e o dia da semana.

Em `PrayerReminder`:

- `minutesFor(prayer, global, overrides): Int`.
- `next(now, city, method, offsets, minutesFor: (Prayer) -> Int)` — a próxima
  notificação é a de menor `horário da oração - minutos daquela oração`.

## Agendamento

- `PrayerScheduler` lê os quatro mapas de override.
- Agenda o lembrete se **qualquer** oração tiver minutos diferentes de zero.
- O `PendingIntent` do lembrete leva `EXTRA_REMINDER_MINUTES` com os minutos
  usados, para o texto da notificação não depender de reler a preferência.
- `PrayerReceiver`:
  - lembrete: usa os minutos do intent; sem eles, o global.
  - adhan: resolve voz, alarme e dias; em dia não repetido não toca nem notifica.
- Trocar qualquer controle na sheet chama `PrayerScheduler(context).reschedule()`.

## UI

### Linha do Hoje

O ícone de sino deixa de alternar e passa a abrir a sheet. O `content-desc`
muda para “Chamado de %s” e o estado ligado segue vindo de `adhanPrayers`.

### Sheet por oração (`PrayerAdhanSheet`)

Título “Chamado de <oração>”. Conteúdo, na ordem do concorrente:

| Linha | Controle | Valor mostrado |
| --- | --- | --- |
| Notificar | switch | ligado/desligado |
| Alarme completo | switch | ligado/desligado (desligado segue a opção global de respeitar silencioso) |
| Repetir | sete chips S M T W T F S | dias efetivos |
| Som | abre a lista | nome da voz efetiva; “padrão” quando herda |
| Lembrete antes | abre a lista | “15 min”, “desligado” ou “padrão” |

Rodapé com **Salvar** (habilitado só quando mudou algo) e fechar.

- Tudo é rascunho local até Salvar.
- Som: “Usar padrão (<voz global>)” no topo, depois as vozes filtradas (Fajr
  mostra as de Fajr e as comuns; as outras só as comuns), com bandeira, país,
  duração e prévia, igual à tela do adhan.
- Lembrete: “Usar padrão (<minutos>)”, desligado, 5, 10, 15 e 30.
- Alarme: um switch. Ligado força o áudio mesmo no silencioso; desligado segue
  o interruptor global “Respeitar silencioso”.
- Repetição: chips com o nome do dia; tocar alterna. Desmarcar todos volta para
  “todos”, com aviso curto.
- Prévia para ao fechar a sheet.

### Tela do adhan (global)

Continua sendo o padrão. Ganha uma linha explicando que cada oração pode ter
exceção e um botão “Ver por oração” que abre a sheet da primeira oração
habilitada. Nada mais muda.

## Strings

Novas, nos três idiomas (en, pt-BR, ar):

- `prayer_adhan_sheet_title` = “<oração> notification settings”
- `prayer_adhan_notify`
- `prayer_adhan_alarm` / `prayer_adhan_alarm_hint`
- `prayer_adhan_repeats` / `prayer_adhan_repeats_all`
- `prayer_adhan_sound_row`
- `prayer_adhan_reminder_row`
- `prayer_adhan_use_default_sound` = “Use default (%1$s)”
- `prayer_adhan_use_default_reminder` = “Use default (%1$s)”
- `prayer_adhan_inherits_default` = “Default”
- `prayer_adhan_off` = “Off”
- `prayer_adhan_save`
- `prayer_adhan_close`
- `prayer_adhan_open` = “Adhan settings for %1$s”
- `adhan_per_prayer_hint`
- `adhan_per_prayer_open`

`LocaleParityTest` quebra o build se faltar alguma.

## Testes

### JVM

- `AdhanOverridesTest`: ida e volta dos quatro mapas; string vazia; lixo; chave
  desconhecida; `SUNRISE` ignorada; voz de Fajr em oração comum descartada; `0`
  preservado; dias fora de 1..7 descartados; conjunto vazio vira todos.
- `AdhanPolicyTest`: `resolveFor` com override, sem override e no Fajr;
  alarme ligado ignora silencioso; dia não repetido não toca.
- `PrayerRemindersTest`: minutos por oração; escolhe o aviso mais próximo;
  oração com `0` não gera aviso; todos `0` devolve `null`.
- `PerPrayerAdhanTest`: `PrayerScheduler` agenda o lembrete certo e a intenção
  leva os minutos.

### E2E (`PrayerAdhanSheetTest`)

1. Abrir a sheet pelo sino do Fajr no Hoje.
2. Desligar Notificar, salvar, reabrir: continua desligado e o sino da linha
   mudou.
3. Escolher uma voz só para o Maghrib; voltar ao Fajr e ver “padrão”.
4. Escolher lembrete de 15 min no Isha e ver o valor na sheet.
5. Ligar o alarme no Fajr e desmarcar segunda-feira na repetição.
6. Fechar sem salvar e confirmar que nada mudou.

## Critérios de aceite

- Sem tocar em nada, o comportamento é idêntico ao de hoje (incluindo a
  exceção do Fajr).
- Som, lembrete, alarme e repetição por oração sobrevivem a reiniciar o app.
- O alarme do lembrete usa os minutos da oração e a notificação mostra o valor
  certo.
- Uma voz de Fajr nunca toca fora do Fajr, nem por override.
- Dia não repetido não toca adhan nem notifica, e o agendamento seguinte
  continua de pé.
- `gradlew test`, `lint`, `connectedDebugAndroidTest` verdes e
  `scripts/install-local.ps1` com `OK` nos emuladores.

## Fora de escopo

- Catálogo remoto, download de vozes, conta ou assinatura.
- Tela cheia de alarme e `USE_FULL_SCREEN_INTENT`.
- Qualquer áudio, string, bandeira ou layout do Muslim Pro.
- Comparativo no Instagram: só depois desta entrega.
