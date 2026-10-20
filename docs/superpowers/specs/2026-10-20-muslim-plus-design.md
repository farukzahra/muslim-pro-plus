# Muslim Plus — design

- Data: 2026-10-20
- Status: aguardando aprovação
- Stack: Kotlin + Jetpack Compose (Android)
- Público: Google Play, Android 8+ (minSdk 26), targetSdk 36

> Nome: **Muslim Plus** · pacote: **`com.farukzahra.muslimplus`** (livre no Play,
> verificado em 2026-10-20; risco de confusão com "Muslim Pro" registrado e
> aceito pelo usuário).

## Por que

App islâmico original: horários de oração, Qibla e Alcorão, com a mesma
arquitetura de informação que o usuário já conhece (5 abas + grade no Mais),
mas com código, marca e assets próprios. Nada do Muslim Pro é reutilizado.

## Escopo v1

| Área | Entrega |
|---|---|
| Hoje | Data gregoriana e hijri, próxima oração com contador, lista do dia, verso do dia |
| Orações | Tabela do mês, método de cálculo, ajustes por oração, localização manual ou GPS |
| Alcorão | 114 suras, texto uthmani offline, áudio por verso, marcador, última leitura, busca |
| Qibla | Bússola com sensor de rotação, declinação magnética, distância até a Caaba, calibração |
| Mais | Configurações, Idioma, Notificações, Sobre, Licenças, Privacidade |
| Onboarding | Idioma (pré-preenchido pelo sistema), localização, método, notificações |
| Notificações | Adhan por oração, agendamento exato com fallback, reagendamento em boot/fuso |

## Não entra na v1

- iOS, web, tablet dedicado (o layout responde, mas não é meta).
- Conta, login, comunidade, loja, streaming, doações.
- Anúncios e assinatura. Nenhum SDK de ads.
- Traduções do Alcorão, tafsir, hadith.
- Analytics de terceiros. Sem rastreamento.
- Localização em background: só em primeiro plano (app aberto/retomado ou
  atualização manual).

## Requisitos de idioma (não negociáveis)

1. Nenhuma string de UI no código. Tudo em `strings.xml`; lint `HardcodedText`
   como erro.
2. Locales v1: `en` (fallback), `pt-rBR`, `ar`. Estrutura pronta para crescer.
3. Primeira execução: sem override, segue o locale do aparelho. Se não houver
   tradução, cai para `en`.
4. Troca no app: "Sistema (padrão)", English, Português (Brasil), العربية.
   Persistida e aplicada via `AppCompatDelegate.setApplicationLocales`.
5. `localeConfig` declarado: o idioma aparece em Configurações do Android 13+.
6. RTL desde o primeiro layout: `start/end`, `supportsRtl`, teste em `ar`.
   O verso árabe é um parágrafo **RTL**; números e referência do verso vão em
   isolamento bidi para não embaralhar a ordem.
7. Conteúdo localizado, não só a casca: nomes de oração, meses hijri, dias da
   semana, formatos de data/hora, métodos de cálculo, nomes de suras
   (transliteração + significado), textos de notificação, erros e vazios.
8. Plurais via `plurals`, nunca concatenação. Números com
   `NumberFormat`/`DateTimeFormatter` do locale.
9. Teste automático de paridade: todas as chaves existem nos 3 locales e
   nenhuma string vazia.

## Arquitetura

- Módulo único `app`, pacotes por domínio (`ui`, `data`, `domain`, `prayer`,
  `quran`, `location`, `settings`, `notifications`). Extrair módulos só quando
  doer.
- Compose + Material 3, Navigation Compose, ViewModel + Flow.
- Hilt para DI, Room (marcadores, última leitura, índice de áudio, FTS do
  texto corânico), DataStore (settings e idioma), kotlinx.serialization.
- `java.time` (minSdk 26 não exige desugaring).
- Cálculo de oração: `adhan-java` (MIT), métodos MWL, ISNA, Egito, Karachi,
  Makkah, Dubai, MoonsightingCommittee, Turquia, Teerã, Kuwait, Qatar,
  Singapura, + offsets manuais por oração. Métodos ausentes no port Java são
  implementados como `CustomMethod` com os ângulos publicados.
- Latitude alta: regra `MoonsightingCommittee` acima de |48°|, com
  `MiddleOfTheNight` como fallback.
- Localização: FusedLocationProvider com fallback para `LocationManager`;
  cidade manual sempre disponível (o app funciona sem GPS). Cidades vêm de um
  catálogo offline (GeoNames `cities5000`, CC BY 4.0) com lat/lon **e fuso
  IANA**; os horários são calculados no fuso da cidade escolhida.
- Bússola: vetor de rotação; declinação via `android.hardware.GeomagneticField`
  (sem tabela WMM embarcada). Sem magnetômetro, mostra o rumo estático e
  explica o motivo.
- Áudio: Media3 + `MediaSessionService` (toca com a tela apagada), um recitador
  na v1, download por sura com cache LRU. Fonte abstraída atrás de uma
  interface para trocar de CDN sem tocar na UI.
- Design system próprio: tokens em `ui/theme` (cor, tipografia, espaçamento),
  tema claro/escuro/sistema. Paleta "meia-noite + areia + esmeralda", sem
  herdar a paleta do Muslim Pro.

## Dados e licenças

| Dado | Fonte | Licença | Como entra |
|---|---|---|---|
| Texto do Alcorão (uthmani) | Tanzil.net | CC BY 3.0, com atribuição | JSON/SQLite embarcado (~1,6 MB) |
| Áudio por verso | everyayah.com | termos não publicados; uso por stream | stream do CDN + cache privado no aparelho, sem rebundling |
| Fonte árabe | Amiri Quran | OFL | embarcada |
| Horários | cálculo local | `adhan-java`, MIT | nada de API externa |
| Catálogo de cidades | GeoNames | CC BY 4.0 | subset embarcado |
| Metadados de suras | Tanzil + nossos | — | árabe, transliteração; significado em pt-BR escrito por nós |

Atribuições em tela "Licenças", linkadas no README e no listing.

O áudio é sempre buscado do CDN de origem (o app não redistribui arquivo). Se
os termos mudarem ou houver reclamação, troca-se a interface de fonte e a tela
desabilita o download — sem refazer o player.

## Telas

1. **Hoje** — cabeçalho com data dupla, card da próxima oração com contador,
   lista com Fajr…Isha + nascer do sol, verso do dia.
2. **Orações** — tabela do mês, botão de localização, método, offsets,
   toggles de notificação por oração.
3. **Alcorão** — busca e lista das 114 suras; busca full-text nos 6.236 versos
   com normalização árabe (sem harakat, unificação de alef/ya) via FTS do Room,
   além de nome/significado da sura. Leitor com número do verso, play por
   verso, bookmark, "continuar de onde parei", download da sura.
4. **Qibla** — bússola, graus, distância, estado de precisão e dica de
   calibração em figura-8.
5. **Mais** — grade com 6 itens reais (não 20 placeholders), versão e licenças.

## Notificações

- `POST_NOTIFICATIONS` (13+) pedido no onboarding.
- `SCHEDULE_EXACT_ALARM` (não `USE_EXACT_ALARM`, que o Play restringe a
  apps de alarme/calendário). Sem permissão, agenda inexato e avisa na UI que
  pode atrasar alguns minutos.
- Uma notificação no horário, por oração ligada. Sem lembrete prévio na v1.
- Reagenda em `BOOT_COMPLETED`, `TIMEZONE_CHANGED`, `TIME_SET` e mudança de
  local/método.
- Som: padrão do sistema na v1; adhan embarcado só com licença verificada.

## Privacidade e Play

- Localização usada só no aparelho, sem envio a servidor. Data safety:
  "não coletado, não compartilhado".
- Sem conta, sem analytics, sem ads.
- Política de privacidade pública em página estática — **em repositório
  separado** deste, para não vincular o app ao histórico de engenharia reversa.
- AAB assinado com Play App Signing; keystore de upload em `../secrets/`.
- Listing pt-BR + en; ar depois da revisão nativa.
- Assets de loja: ícone adaptativo + 512, feature graphic 1024×500 e 4
  screenshots por idioma, gerados por mim.

## Critérios de aceite

- Em modo avião: horários, Alcorão (texto) e Qibla funcionam.
- Trocar idioma muda a UI na hora e sobrevive ao restart; primeira execução
  segue o aparelho.
- Nenhuma tela mistura idiomas; lint de string e teste de paridade passam.
- `ar` sem corte, sobreposição ou espelhamento errado.
- Notificação dispara a ±1 min quando exato está permitido; degrada com aviso.
- Upload na trilha interna passa na revisão (data safety, classificação).

## Decisões do construtor (revise)

- Hilt, Room, módulo único, sem ads/analytics/conta.
- Onboarding pulável: usa a última localização conhecida; se não houver, a
  cidade de referência do fuso do aparelho; Makkah só em último caso.
- Cidades com fuso IANA (horário calculado no fuso da cidade, não do aparelho).
- Busca do Alcorão com FTS e normalização árabe.
- Áudio com Media3 em serviço, um recitador, cache LRU, fonte trocável.
- Qibla com `GeomagneticField`; sem sensor, rumo estático explicado.
- Notificação só no horário (sem pré-aviso).
- Verso do dia: lista curada embarcada, por idioma, rotacionada pela data —
  o mesmo modelo editorial do Muslim Pro (feed `didyouknow` servido por eles),
  sem precisar de backend. Lista inicial preparada por mim, revisada por você.

## Decisão registrada

**Nome e pacote** — "Muslim Plus" já é usado por 6 apps no Play (um com 1M+
downloads) e fica a um passo de "Muslim Pro". O usuário optou por manter o nome
e o pacote `com.farukzahra.muslimplus`, ciente do risco. O pacote é permanente;
o título pode ser revisto depois com revisão do Play.

**Política de privacidade** — página estática pronta (pt-BR, en, ar) em
`C:\repo\muslim-plus-site`, publicada em `/opt/muslimplus` na VPS com bloco
Caddy para `muslimplus.faruk.dev.br`. O registro A **não** foi criado por
decisão do usuário em 2026-10-20; o subdomínio só responde depois disso.

## Riscos

| Risco | Mitigação |
|---|---|
| Termos do everyayah | stream sem redistribuir; fonte trocável; download desligável |
| Política de alarme exato | `SCHEDULE_EXACT_ALARM` + fallback inexato com aviso |
| Precisão hijri | `HijrahDate` Umm al-Qura + offset manual de ±1 dia |
| Tradução árabe | revisão nativa antes do release |
| Conta de dev no Play | $25 + verificação; bloqueia publicação, não o build |
