# Adhan de Fajr, bandeiras e catálogo de 22 vozes

Data: 01/11/2026. Status: **proposto; aguardando aprovação do spec**.
Pesquisa: [`docs/research/2026-11-01-adhan-fajr-concorrentes.md`](../../research/2026-11-01-adhan-fajr-concorrentes.md).
Página de audição: [`docs/adhan-preview/index.html`](../../adhan-preview/index.html).

## Por que

O pai do usuário pediu um ou dois adhan de Fajr com
<span lang="ar">الصلاة خير من النوم</span> (“a oração é melhor que o sono”,
tathwib). O usuário pediu mais vozes, bandeira do país em cada voz, e decidiu
que o Fajr deve vir ligado e já setado com uma gravação de Fajr.

A pesquisa mostrou que duas das nove gravações atuais já são de Fajr e hoje
podem ser escolhidas para qualquer oração. Isso será corrigido nesta entrega.

## Decisões aprovadas em 01/11/2026

1. Configuração **padrão + Fajr**, não cinco seletores por oração.
2. Catálogo **quanto mais melhor**: 22 vozes, 13 novas.
3. **Fajr ligado por padrão**, com `madinah-nafman` e override ligado.
4. **Reclassificar** as duas gravações de Fajr e migrar quem as usava no dia.
5. **Bandeira do país** em cada voz, no estilo do Muslim Pro.

## Escopo

### Entra

- 13 gravações novas em `res/raw`, convertidas para mono 44,1 kHz, 48 kbps.
- `AdhanSound` com país, bandeira e marcação de Fajr.
- Tela do adhan com cartão de gravação padrão, cartão do Fajr e bottom sheet de
  escolha com bandeira, nome, país, duração e prévia.
- Fajr ligado por padrão e já setado com Madinah, Faisal Al-Na’man.
- Migração de quem tinha uma gravação de Fajr como voz global.
- Aviso único de migração.
- Strings nos três idiomas, incluindo nomes dos países.
- Atribuições em Licenças.

### Não entra

- Cinco seletores independentes, um por oração. Fica para uma segunda etapa.
- Download de áudio pela rede.
- Cortar o tathwib das gravações atuais para criar versões “normais”.
- Qualquer áudio, layout, string ou bandeira do Muslim Pro.
- Mudança nos gatilhos de parada (volume, tela, rotação).

## Catálogo final

22 vozes: 16 para o dia e 6 de Fajr. A lista completa, com duração e fonte,
está na pesquisa e na página. Resumo:

| Uso | Quantidade | Vozes |
| --- | --- | --- |
| Dia | 16 | clássico; 3 Makkah; Al-Aqsa; Abdulaziz Bukhari; Khashoggi; Qatami; Al-Banna; Hamathani; Shuaisha; Ajman; Tunísia; Omã; Mokhtar; Imadi |
| Fajr | 6 | Faisal Al-Na’man; Essam Bukhari; Makkah Fajr; Cairo Fajr; Malásia Fajr; Umm al-Qaiwain Fajr |

### Países e bandeiras

| País | Código | Vozes |
| --- | --- | --- |
| Arábia Saudita | `sa` | Makkah, Madinah, Qatami, Hamathani |
| Palestina | `ps` | Al-Aqsa |
| Egito | `eg` | Cairo (Al-Banna, Shuaisha, Fajr) |
| Emirados Árabes | `ae` | Ajman, Umm al-Qaiwain |
| Tunísia | `tn` | Tunísia |
| Omã | `om` | Omã |
| Malásia | `my` | Malásia Fajr |
| Argélia | `dz` | Mokhtar Hadj Slimane |
| Catar | `qa` | Ahmed Al-Imadi |

O clássico não tem país e usa um ícone neutro (nota musical). As bandeiras são
PNG de domínio público do flagcdn.com, em `res/drawable-nodpi/flag_<code>.png`,
24 × 16 dp com cantos arredondados.

## Modelo de dados

```kotlin
enum class AdhanCountry(
    val code: String,
    @StringRes val labelRes: Int,
    @DrawableRes val flagRes: Int,
) {
    SAUDI_ARABIA("sa", R.string.country_saudi_arabia, R.drawable.flag_sa),
    PALESTINE("ps", R.string.country_palestine, R.drawable.flag_ps),
    EGYPT("eg", R.string.country_egypt, R.drawable.flag_eg),
    UAE("ae", R.string.country_uae, R.drawable.flag_ae),
    TUNISIA("tn", R.string.country_tunisia, R.drawable.flag_tn),
    OMAN("om", R.string.country_oman, R.drawable.flag_om),
    MALAYSIA("my", R.string.country_malaysia, R.drawable.flag_my),
    ALGERIA("dz", R.string.country_algeria, R.drawable.flag_dz),
    QATAR("qa", R.string.country_qatar, R.drawable.flag_qa),
}

enum class AdhanSound(
    val id: String,
    @RawRes val resId: Int,
    @StringRes val labelRes: Int,
    val country: AdhanCountry?,
    val fajrOnly: Boolean,
)
```

Regras:

- `AdhanSound.normal`: `fajrOnly == false`.
- `AdhanSound.fajr`: `fajrOnly == true`.
- `default` = `CLASSIC`.
- `defaultFajr` = `MADINAH_NAFMAN`.
- `find(id)` devolve a entrada; se o id não existir, devolve o padrão.
- `findFajr(id)` devolve a entrada só se `fajrOnly`; senão, `defaultFajr`.
- `findNormal(id)` devolve a entrada só se `!fajrOnly`; senão, `default`.

## Preferências

| Chave | Tipo | Padrão |
| --- | --- | --- |
| `adhan_prayers` | `Set<String>` | as cinco orações, incluindo Fajr |
| `adhan_sound` | `String` | `classic` |
| `adhan_fajr_sound` | `String` | `madinah-nafman` |
| `adhan_fajr_override` | `Boolean` | `true` |
| `adhan_fajr_migration_notice` | `Boolean` | `false` |

`AdhanPolicy.defaultEnabled` passa a ser `eligible.toSet()` (todas as cinco).
Quem já tem a chave `adhan_prayers` salva mantém o conjunto salvo. Quem nunca
mexeu recebe o novo padrão com Fajr ligado.

## Resolução do som

```kotlin
fun resolve(
    prayer: Prayer,
    global: AdhanSound,
    fajr: AdhanSound,
    override: Boolean,
): AdhanSound =
    if (prayer == Prayer.FAJR && override) {
        if (fajr.fajrOnly) fajr else AdhanSound.defaultFajr
    } else {
        if (!global.fajrOnly) global else AdhanSound.default
    }
```

`PrayerReceiver` lê as quatro preferências, resolve e chama
`AdhanService.start(context, prayer, sound)`. Nada muda no serviço.

## Migração

Na primeira leitura após a atualização:

1. Se `adhan_sound` aponta para uma gravação `fajrOnly`:
   - gravar `adhan_fajr_sound` com esse id;
   - gravar `adhan_sound = classic`;
   - gravar `adhan_fajr_override = true`;
   - gravar `adhan_fajr_migration_notice = true`.
2. Se `adhan_fajr_sound` aponta para id desconhecido ou não-Fajr, usar
   `madinah-nafman`.
3. Se `adhan_prayers` é nulo, usar as cinco.
4. Não interromper áudio em reprodução.

A tela do adhan mostra um cartão único quando `adhan_fajr_migration_notice` é
verdadeiro:

> “Sua gravação de Madinah é de Fajr. Ela agora toca no Fajr. Para o dia,
> escolha outra gravação.”

Com um botão “Entendi” que grava `false`. Texto nos três idiomas.

## Tela do adhan

Estrutura:

```
Configurações → Chamado (adhan)

Orações
  Fajr      [switch ligado]
  Dhuhr     [switch ligado]
  Asr       [switch ligado]
  Maghrib   [switch ligado]
  Isha      [switch ligado]

Gravação padrão
  [bandeira] Adhan clássico
  Gravação anônima · 2:34              [Trocar]

Fajr
  Usar uma gravação diferente no Fajr  [switch ligado]
  [bandeira] Madinah — Faisal Al-Na’man
  Masjid an-Nabawi · 5:48              [Trocar]

Lembrete antes do adhan  [chips]
Respeitar modo silencioso [switch]
Aviso de volume / parada
```

O cartão “Trocar” abre um `ModalBottomSheet`:

- Título “Escolher gravação” ou “Escolher gravação do Fajr”.
- Lista com uma linha por voz: bandeira, nome, país/mesqueta, duração,
  botão de prévia e indicador de seleção.
- A folha do padrão lista só `normal`; a do Fajr lista só `fajr`.
- A prévia usa `MediaPlayer` e para ao fechar a folha.
- Selecionar grava na hora, fecha a folha e atualiza o cartão.
- Sem reschedule: o som é lido no disparo do alarme.

Por que bottom sheet: com 22 vozes, a lista inline atual ficaria com 38 linhas
na mesma tela. A folha mantém a tela principal curta e é o padrão visual do
Muslim Pro e dos concorrentes.

## Áudio

Arquivos novos:

| Arquivo | Origem |
| --- | --- |
| `adhan_fajr_makkah.mp3` | `2nd Fajr Haram Makkah best.mp3` |
| `adhan_fajr_cairo.mp3` | `Cairo AlFajr.mp3` |
| `adhan_fajr_malaysia.mp3` | `Malaysia Al Fajr.mp3` |
| `adhan_fajr_umm_al_qaiwain.mp3` | `AlFajr Umm alQaiwain.mp3` |
| `adhan_qatami.mp3` | `Nasser_al_Qatami_Adhan.mp3` |
| `adhan_cairo_banna.mp3` | `Cairo Mohammed Al Banna.mp3` |
| `adhan_madinah_hamathani.mp3` | `Majed_al_Hamathani_Adhan.mp3` |
| `adhan_cairo_shuaisha.mp3` | `Cairo Shuaisha.mp3` |
| `adhan_ajman.mp3` | `Ajman of UAE.mp3` |
| `adhan_tunisia.mp3` | `Tunisia.mp3` |
| `adhan_oman.mp3` | `Oman.mp3` |
| `adhan_maghreb_mokhtar.mp3` | `Mokhtar_Hadj_Slimane_Adhan.mp3` |
| `adhan_qatar_imadi.mp3` | `Ahmed_al_Imadi_Adhan.mp3` |

Conversão:

```powershell
ffmpeg -i entrada -ac 1 -ar 44100 -b:a 48k `
  -af loudnorm=I=-16:TP=-1.5:LRA=11 saida.mp3
```

Conferir duração e que o tathwib segue audível nas quatro de Fajr. As durações
e os segundos do tathwib estão na página.

## Strings

Novas, nos três locales:

- `adhan_sound_default_title`, `adhan_sound_default_hint`
- `adhan_sound_fajr_title`, `adhan_sound_fajr_hint`
- `adhan_sound_override`, `adhan_sound_override_hint`
- `adhan_sound_change`, `adhan_sound_choose`, `adhan_sound_choose_fajr`
- `adhan_sound_selected`, `adhan_sound_tathwib`
- `adhan_fajr_note`, `adhan_fajr_note_action`
- `country_saudi_arabia`, `country_palestine`, `country_egypt`,
  `country_uae`, `country_tunisia`, `country_oman`, `country_malaysia`,
  `country_algeria`, `country_qatar`

`LocaleParityTest` cobre os três idiomas. Nenhuma string no código.

## Licenças

- Azaan of World Mosques: mesma marca de domínio público já registrada; as
  novas gravações entram no mesmo item.
- Adhan Notifications (archive.org): novo item, marca de domínio público
  declarada; registrar no Licenças.
- Bandeiras: flagcdn.com / Wikimedia Commons, domínio público; registrar.
- Nenhum asset do Muslim Pro.

## Testes

### JVM

- `AdhanPolicyTest`:
  - `defaultEnabled` contém as cinco orações.
  - `sanitize(null)` devolve as cinco; `sanitize(emptySet())` devolve vazio.
  - `resolve` do Fajr com override devolve a voz de Fajr.
  - `resolve` do Fajr sem override devolve a padrão.
  - `resolve` do Dhuhr devolve a padrão mesmo com override ligado.
  - Voz de Fajr no lugar da padrão cai em `classic`; voz normal no lugar da
    Fajr cai em `madinah-nafman`.
- `AdhanSoundTest`:
  - 22 entradas, ids únicos.
  - 16 normais e 6 de Fajr.
  - Toda voz de Fajr tem `fajrOnly`.
  - Toda voz com país tem `flagRes` não zero.
  - `defaultFajr` é Fajr.
- `AdhanMigrationTest`: id de Fajr no `adhan_sound` vira `adhan_fajr_sound`,
  `adhan_sound` vira clássico e o aviso fica pendente.

### Instrumentado

- Abrir o adhan: Fajr ligado; cartão padrão clássico; cartão Fajr Madinah
  Numan.
- Trocar a padrão na folha: selecionar Makkah Mulla, conferir o cartão,
  fechar e reabrir o app.
- Trocar a do Fajr: selecionar Makkah Fajr, conferir o cartão, fechar e
  reabrir.
- Desligar o override: o cartão do Fajr mostra a padrão e a folha do Fajr fica
  indisponível.
- Prévia toca e para ao fechar a folha.
- Bandeira visível em cada linha.
- `PrayerReceiver` de Fajr inicia o serviço com a voz de Fajr; Dhuhr com a
  padrão.

### Manual

- Pixel 6: ouvir as seis vozes de Fajr e confirmar o tathwib.
- `install-local.ps1` com `OK <serial>`.
- Conferir tamanho do APK/AAB.

## Critérios de aceite

- 22 vozes no catálogo, todas com prévia.
- 6 vozes de Fajr, todas com tathwib confirmado.
- Nenhuma voz de Fajr aparece na lista do dia.
- Bandeira em cada voz com país; clássico com ícone neutro.
- Fajr ligado por padrão e setado com Madinah Numan.
- Quem tinha uma voz de Fajr no dia recebe migração e aviso.
- Strings nos três idiomas; `LocaleParityTest` verde.
- `test`, `lint`, E2E e `install-local` verdes.
- Licenças atualizadas.

## Riscos

- **Tamanho:** +14 MB de áudio, ~26 MB no total. Se o AAB passar do aceitável,
  cortar Shuaisha, Omã e Imadi primeiro.
- **Fajr audível:** ligar por padrão muda o comportamento de quem nunca abriu a
  tela. As notas da versão precisam avisar.
- **Licença:** a marca de domínio público do archive.org é auto-declarada; é a
  mesma situação das nove atuais. Manter atribuição e não copiar do Muslim Pro.
- **Vozes sem nome:** Makkah Fajr, Cairo Fajr, Malásia, Umm al-Qaiwain e as
  gravações por cidade não têm muezzin confirmado. Rotular por cidade/país, não
  inventar nome.

## O que fica para depois

- Per-prayer completo (uma voz por oração).
- Busca/filtro no bottom sheet quando o catálogo passar de 30 vozes.
- Versões “normais” das gravações de Fajr, se alguém quiser a mesma voz no dia.
- Mais países do acervo (Istambul, Bósnia, Turquia) depois de avaliar licença e
  qualidade.
