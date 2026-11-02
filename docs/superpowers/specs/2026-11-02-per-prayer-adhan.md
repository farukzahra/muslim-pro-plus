# Adhan por oração — som e lembrete com exceção

Data: 02/11/2026. Status: **proposto, aguardando aprovação**.
Pesquisa: [`docs/research/2026-11-02-muslim-pro-paid-features.md`](../../research/2026-11-02-muslim-pro-paid-features.md).
Substitui a decisão “cinco seletores por oração ficam para a segunda etapa” do
spec [`2026-11-01-adhan-fajr-e-catalogo.md`](2026-11-01-adhan-fajr-e-catalogo.md).

## Por que

O Muslim Pro deixa escolher, **por oração**, o som do adhan e o lembrete antes
dele. Hoje o Muslim Plus tem só duas coisas: liga/desliga por oração
(`adhanPrayers`) e uma voz global com exceção do Fajr. Quem quer o adhan da
Makkah no Maghrib e o de Madinah no Fajr não consegue.

O pedido do usuário foi “pegar as funcionalidades pagas do Muslim Pro e
implementar aqui”. A parte paga que dá para fazer sem servidor é esta.

## Decisões

1. **Exceção por oração sobre o padrão**, não cinco configurações
   independentes. Quem não mexer em nada continua com o comportamento de hoje.
2. **Som e lembrete separados**: dá para trocar só o som, só o lembrete, os
   dois ou nenhum.
3. **A sheet substitui o toggle direto** no ícone de sino da linha do Hoje. O
   sino continua mostrando ligado/desligado.
4. **Salvar é explícito**. Fechar a sheet descarta, como no concorrente.
5. **Sem conta, sem download, sem áudio novo**: as 22 vozes embarcadas
   continuam sendo o catálogo.
6. **Fase 2 fora desta entrega**: “alarme completo” e repetição por dia da
   semana.

## Modelo de dados

Duas chaves novas no DataStore. Nenhuma migração: ausência = herda o global.

| Chave | Formato | Exemplo |
| --- | --- | --- |
| `adhan_sound_by_prayer` | `ORAÇÃO=id;…` | `FAJR=madinah-nafman;ISHA=makkah-mulla` |
| `adhan_reminder_by_prayer` | `ORAÇÃO=minutos;…` | `FAJR=15;ISHA=0` |

`0` no lembrete significa “desligado só nesta oração”. Oração sem entrada herda
o valor global.

## Regras puras

Novo objeto `adhan/AdhanOverrides.kt`, sem Android:

- `decodeSounds(raw: String?): Map<Prayer, String>` — ignora `SUNRISE`, id
  desconhecido e voz de Fajr em oração comum (e o contrário). Sobra só o que é
  válido para aquela oração.
- `encodeSounds(map): String` — ordem fixa de `Prayer.entries`, separador `;`,
  sem espaços.
- `decodeReminders(raw: String?): Map<Prayer, Int>` — descarta valor fora de
  `PrayerReminder.options`.
- `encodeReminders(map): String` — mesma ordenação.

Em `AdhanPolicy`:

- `resolveFor(prayer, global, fajr, fajrOverride, overrides): AdhanSound` —
  override da oração quando existe e é válido; senão cai em `resolve(...)`.

Em `PrayerReminder`:

- `minutesFor(prayer, global, overrides): Int` — override da oração, senão
  global.
- `next(now, city, method, offsets, minutesFor: (Prayer) -> Int)` — a próxima
  notificação é a de menor `horário da oração - minutos daquela oração` entre as
  cinco. A versão atual com minutos fixos passa a delegar para esta.

## Agendamento

- `PrayerScheduler` lê os dois mapas de override.
- Agenda o lembrete se **qualquer** oração tiver minutos diferentes de zero.
- O `PendingIntent` do lembrete leva `EXTRA_REMINDER_MINUTES` com os minutos
  usados, para o texto da notificação (“em 15 minutos”) não depender de reler a
  preferência depois de uma mudança.
- `PrayerReceiver` usa os minutos do intent; se faltarem, cai no global.
- Trocar som ou lembrete na sheet chama `PrayerScheduler(context).reschedule()`.

## UI

### Linha do Hoje

O ícone de sino deixa de alternar e passa a abrir a sheet. O `content-desc`
muda para “Chamado de %s” e o estado ligado segue vindo de `adhanPrayers`.

### Sheet por oração (`PrayerAdhanSheet`)

Título “Chamado de <oração>”. Conteúdo, na ordem do concorrente:

| Linha | Controle | Valor mostrado |
| --- | --- | --- |
| Notificar | switch | ligado/desligado |
| Som | abre a lista | nome da voz efetiva; “padrão” quando herda |
| Lembrete antes | abre a lista | “15 min”, “desligado” ou “padrão” |

Rodapé com **Salvar** (habilitado só quando mudou algo) e fechar.

- Tudo é rascunho local até Salvar.
- Som: lista com “Usar padrão (<voz global>)” no topo, depois as vozes
  filtradas (Fajr mostra as de Fajr e as comuns; as outras só as comuns), com
  bandeira, país, duração e prévia, igual à tela do adhan.
- Lembrete: “Usar padrão (<minutos>)”, desligado, 5, 10, 15 e 30.
- Prévia para ao fechar a sheet.

### Tela do adhan (global)

Continua sendo o padrão. Ganha uma linha explicando que cada oração pode ter
exceção e um botão “Ver por oração” que abre a mesma sheet da primeira oração
habilitada. Nada mais muda.

## Strings

Novas, nos três idiomas (en, pt-BR, ar):

- `prayer_adhan_sheet_title` = “<oração> notification settings”
- `prayer_adhan_notify`
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

`LocaleParityTest` quebra o build se faltar alguma.

## Testes

### JVM

- `AdhanOverridesTest`: ida e volta, string vazia, lixo, chave desconhecida,
  `SUNRISE` ignorada, voz de Fajr em oração comum descartada, `0` preservado.
- `AdhanPolicyTest`: `resolveFor` com override, sem override e no Fajr.
- `PrayerRemindersTest`: minutos por oração; escolhe o aviso mais próximo;
  oração com `0` não gera aviso; todos `0` devolve `null`.
- `PerPrayerAdhanTest`: `PrayerScheduler` agenda o lembrete certo e o
  `intent` leva os minutos (teste de intenção, como `RescheduleIntentsTest`).

### E2E (`PrayerAdhanSheetTest`)

1. Abrir a sheet pelo sino do Fajr no Hoje.
2. Desligar Notificar, salvar, reabrir: continua desligado e o sino da linha
   mudou.
3. Escolher uma voz só para o Maghrib; voltar ao Fajr e ver “padrão”.
4. Escolher lembrete de 20 min no Isha e ver o valor na linha da sheet.
5. Fechar sem salvar e confirmar que nada mudou.

## Critérios de aceite

- Sem tocar em nada, o comportamento é idêntico ao de hoje (incluindo a
  exceção do Fajr).
- Som e lembrete por oração sobrevivem a reiniciar o app.
- O alarme do lembrete usa os minutos da oração e a notificação mostra o valor
  certo.
- Uma voz de Fajr nunca toca fora do Fajr, nem por override.
- `gradlew test`, `lint`, `connectedDebugAndroidTest` verdes e
  `scripts/install-local.ps1` com `OK` nos emuladores.

## Fora de escopo

- “Alarme completo” (tocar o adhan inteiro por cima do modo silencioso) e
  repetição por dia da semana.
- Catálogo remoto, download de vozes, conta ou assinatura.
- Qualquer áudio, string, bandeira ou layout do Muslim Pro.
- Comparativo no Instagram: só depois desta entrega.
