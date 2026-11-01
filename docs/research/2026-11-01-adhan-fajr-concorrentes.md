# Adhan de Fajr e catálogo de vozes — como os concorrentes fazem

Data: 01/11/2026. Pesquisa e proposta. **Nada implementado no app.**

Página de audição: [`docs/adhan-preview/index.html`](../adhan-preview/index.html).

## Pedido

O pai do usuário pediu “um ou dois azan dizendo
<span lang="ar">الصلاة خير من النوم</span> — *assalatu khairun mina annaum*,
‘a oração é melhor que o sono’ — e perguntou como escolher o adhan do Fajr.
O usuário pediu mais tipos de adhan e quer decidir a configuração antes de
implementar.

## Resposta curta

1. O Fajr é o único adhan com o tathwib. Não é um “tipo” de voz: é uma
   gravação própria, recitada depois de “hayya ‘ala al-falah”.
2. Todos os concorrentes relevantes deixam escolher o som **por oração**.
   Muslim Pro faz isso com uma entrada “Adhan Fajr (…)” no mesmo catálogo.
3. Para o nosso app, o modelo mais simples que resolve o pedido é
   **gravação padrão + exceção do Fajr**. O modo “uma voz por oração” fica
   como opção avançada.
4. **Duas das nove gravações já embarcadas são adhan de Fajr** e hoje podem
   ser escolhidas para qualquer oração. Isso precisa ser corrigido de qualquer
   forma.

## Evidência no Muslim Pro 17.10.1

APK em `old/apk/muslimpro-17.10.1.apk`; recursos decodificados em
`build/muslimpro/apktool/` (fora do git).

### Catálogo

`assets/content/adhans.json` tem 31 gravações. Seis são explicitamente de
Fajr, todas no mesmo catálogo, com `type: 3`:

| id | nome | arquivo | duração |
| --- | --- | --- | --- |
| 5 | Adhan Fajr (Madina) | `adhan_fajr_madina.mp3` | 5:19 |
| 8 | Adhan Fajr (Mansour Zahrani Fajr) | `adhan_fajr_mansour_zahrani.mp3` | 3:04 |
| 20 | Adhan Fajr (Malek Chebae) | `adhan_fajr_malek_chebae.mp3` | 4:02 |
| 32 | Adhan Fajr (Muzammil) | `adhan_fajr_muzammil.mp3` | 4:50 |
| 33 | Adhan Fajr (Salim Bahanan) | `adhan_fajr_salim_bahanan.mp3` | 3:38 |
| 35 | Adhan Fajr (Syam Elmarusy) | `adhan_ustad_syam_adzan_subuh.mp3` | 4:14 |

O catálogo também tem “None”, “Silent”, “Beep” e “Longer beep” como opções.
Ou seja: o Fajr não é um modo separado; é uma gravação marcada no nome e
escolhida como qualquer outra.

### Configuração

- `res/xml/settings_adhan_notification.xml` lista: volume de alarme, “Force
  Adhan notifications”, vibrar, LED, diagnóstico, testar adhan e “reset
  notifications”.
- Textos do app:
  - “Dismiss or mute adhans per prayer; customise adhans or silent alerts”
    (`2132083471`).
  - “Adjust notifications for each prayer time” (`2132086201`).
  - “You can select different adhan voices and options later from the
    Settings menu.” (`2132086197`).
- Na tela Orações, cada linha tem um sino/silêncio próprio (capturas
  `build/muslimpro/live-prayers*.png`).
- O suporte oficial confirma: “select individual alerts for each of the five
  prayers and for sunrise” e “change the notification sound to your preferred
  adhan”.
- O som do adhan é um recurso Premium em parte do catálogo, mas a escolha por
  oração não é.

### O que isso significa

O Muslim Pro **não** tem “um adhan global e outro só para o Fajr” como tela.
Ele tem cinco configurações independentes; o Fajr é uma delas. Na prática, o
usuário escolhe uma voz para o dia e uma voz diferente para o Fajr. O catálogo
faz o resto.

## Evidência nos outros apps

| App | Catálogo | Configuração | Fajr |
| --- | --- | --- | --- |
| Athan Pro (Quanticapps) | vários azans para baixar | Configurações → Sons → escolhe a oração → aplica | uma das orações no seletor |
| Athan (applabps) | vozes com download | Settings → Athan Voices → oração → voz | voz por oração, inclusive Fajr |
| Ezanım / SalahNow | 16 gravações | som por oração; pode silenciar uma oração | marca as gravações adequadas a Fajr |
| Salah Clock | 10 vozes | cinco interruptores, um por oração | usa a versão Fajr da voz automaticamente, se existir |
| FirdawsWay | 65 gravações de 26 países | som por oração; “aplicar a todas” depois de editar | lista de Fajr começa pelas que têm tathwib; usa o Fajr do próprio muezzin |

Padrões que se repetem:

1. **Por oração** é o padrão de mercado.
2. **Fajr separado** é o caso de uso mais citado.
3. Quem tem catálogo grande **emparelha** a voz normal com a versão Fajr e usa
   a versão Fajr automaticamente.
4. Quem tem catálogo pequeno **marca** as gravações de Fajr e deixa o usuário
   escolher.

## O problema no nosso catálogo

Analisei as nove gravações embarcadas com transcrição automática (Gemini) e
confirmei o tathwib em duas:

| Gravação | Tathwib | Confiança |
| --- | --- | --- |
| Madinah — Faisal Al-Na’man (`adhan_madinah_nafman.mp3`) | 4:19 e 4:44 | 1,0 |
| Madinah — Essam Bukhari (`adhan_madinah_bukhari.mp3`) | 4:15 e 4:34 | 0,98 |
| As outras sete | não | 1,0 |

Hoje as duas são oferecidas para Dhuhr, Asr, Maghrib e Isha. Quem escolhe uma
delas ouve “a oração é melhor que o sono” no meio da tarde. Não é um erro
grave, mas é o tipo de detalhe que um pai que reza cinco vezes por dia percebe.

A correção não exige áudio novo: basta marcar as duas como Fajr e oferecer uma
voz de Madinah sem tathwib para o dia. Majed Al-Hamathani resolve.

## Proposta de configuração

### Modelo recomendado: padrão + Fajr

```
Gravação padrão ......... Dhuhr, Asr, Maghrib, Isha
[ ] Usar outra no Fajr ... Fajr
```

- Uma lista de gravações normais para o dia.
- Um interruptor “usar outra no Fajr”.
- Quando ligado, uma lista só de gravações de Fajr.
- Quando desligado, o Fajr usa a gravação padrão (comportamento atual).
- O switch de ligar/desligar o adhan por oração continua separado.

Vantagens: uma tela, duas listas, migração simples, cobre o pedido do pai.
Desvantagem: não permite, por exemplo, uma voz para o Maghrib e outra para o
Isha. Ninguém pediu isso ainda.

### Modelo completo: por oração

Cinco seletores, um por oração. É o que o Muslim Pro faz. Vantagens: controle
total e paridade de mercado. Desvantagens: mais tela, mais estado, mais teste,
e a maioria dos usuários não vai mexer.

**Recomendação:** começar com padrão + Fajr. Se houver pedido, o modelo por
oração entra depois sem quebrar o armazenamento (o padrão vira o valor inicial
das cinco orações).

## Catálogo recomendado

Manter as nove atuais, reclassificar duas como Fajr e acrescentar seis:

| Voz | Uso | Duração | Fonte | Licença |
| --- | --- | --- | --- | --- |
| Madinah — Faisal Al-Na’man | Fajr (já está no app) | 5:48 | Azaan of World Mosques | PD Mark |
| Madinah — Essam Bukhari | Fajr (já está no app) | 5:25 | Azaan of World Mosques | PD Mark |
| Makkah — Haram (Fajr) | Fajr novo | 3:35 | Azaan of World Mosques | PD Mark |
| Cairo (Fajr) | Fajr novo | 4:07 | Azaan of World Mosques | PD Mark |
| Malásia (Fajr) | Fajr novo | 3:19 | Azaan of World Mosques | PD Mark |
| Kuwait — Nasser Al-Qatami | dia | 2:12 | Adhan Notifications | PD Mark |
| Cairo — Mohammed Al-Banna | dia | 2:35 | Azaan of World Mosques | PD Mark |
| Madinah — Majed Al-Hamathani | dia | 3:39 | Adhan Notifications | PD Mark |

Total: 15 gravações (10 para o dia + 5 de Fajr, contando as 9 atuais). O áudio
sai de ~12 MB para ~18 MB, convertendo as novas para mono 48 kbps.

Opções que ficam de fora por enquanto:

- Mishary Alafasy e Al-Hosari: são recitadores, não muezzins.
- Makkah Haram 1, 3 e 5: voz não identificada; a 5 tem bitrate de 36 kbps.
- Cairo Shuaisha, Ajman, Tunísia, Omã, Mokhtar, Ziyaad Patel: bons candidatos
  para uma segunda onda, sem espaço no orçamento atual.
- Madinah Fajr CC0: mesma voz da gravação já embarcada; serve como fonte de
  licença mais clara, mas não acrescenta voz.

## Migração

O app já está publicado (1.2.0). Quem escolheu Madinah Numan ou Bukhari como
voz global precisa de um caminho:

1. Ler `adhan_sound`.
2. Se for uma gravação marcada como Fajr, gravar essa gravação em
   `adhan_fajr_sound` e trocar `adhan_sound` para `classic`.
3. Mostrar um aviso único na tela do adhan: “Sua gravação de Madinah é de Fajr;
   ela agora toca no Fajr. Para o dia, escolha outra.”
4. Não interromper um adhan que já está tocando.

Sem essa migração, o usuário continua com o problema. Com ela, perde a voz
global que tinha, mas ganha a voz certa no Fajr. O aviso explica.

## Testes que a implementação exige

- JVM: resolução do som por oração (Fajr com override, Fajr sem override,
  Dhuhr sempre no padrão), ids desconhecidos caem no padrão, gravação de Fajr
  não aparece na lista normal.
- JVM: migração do id antigo para `adhan_fajr_sound`.
- Instrumentado: trocar o padrão, ligar o override, trocar o som do Fajr,
  ouvir prévia, reiniciar o app e conferir persistência.
- Instrumentado: `PrayerReceiver` de Fajr inicia o serviço com a gravação de
  Fajr; Dhuhr inicia com a padrão.
- `AdhanSoundTest`: cada novo `resId` existe em `res/raw`, ids únicos, todas as
  gravações de Fajr têm `tathwib = true` no metadado (teste de catálogo, não de
  áudio).
- `install-local.ps1` com `OK <serial>` no Pixel 6.

## Decisões pendentes do usuário

1. **Modelo:** padrão + Fajr (recomendado) ou cinco seletores por oração?
2. **Fajr padrão:** Faisal Al-Na’man (5:48) ou Essam Bukhari (5:25)?
3. **Tamanho:** acrescentar as seis novas (15 vozes, ~18 MB) ou só as duas de
   Fajr (11 vozes, ~15 MB)?
4. **Fajr ligado por padrão?** Hoje começa desligado. Manter desligado
   (recomendado) ou ligar para novas instalações?
5. **Corrigir as duas gravações de Fajr agora?** Recomendado. Se não, elas
   continuam disponíveis para o dia e o problema permanece.

## Fontes

- Muslim Pro 17.10.1: `old/apk/muslimpro-17.10.1.apk`,
  `build/muslimpro/apktool/assets/content/adhans.json`,
  `build/muslimpro/apktool/res/xml/settings_adhan_notification.xml`,
  `build/muslimpro/live-prayers*.png`.
- Suporte Muslim Pro:
  https://support.muslimpro.com/help/en/articles/how-to-set-prayer-notifications-or-adhan
- Athan Pro:
  https://quanticapps.zendesk.com/hc/en-us/articles/115003758129-How-to-change-the-Athan-sound
- Athan (applabps): https://athan.applabps.com/resources/help/faq/en/
- Ezanım: https://ezanim.app/features/athan-sounds
- Salah Clock: https://salahclock.app/docs/clocks/adhan-audio
- FirdawsWay: https://firdawsway.app/fr/help/adhan-change-sound
- Acervo de áudio:
  https://archive.org/details/azaan-of-world-mosques_202608 e
  https://archive.org/details/adhan.notifications
- Madinah Fajr CC0: https://archive.org/details/MadinahFajrAzan
- Tathwib: https://fiqh.islamonline.net/en/at-tathweeb-saying-prayer-is-better-than-sleep-in-the-morning-adhan/
