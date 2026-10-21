# AGENTS.md — Muslim Plus

App islâmico original para Android (Kotlin + Jetpack Compose): horários de
oração, Qibla e Alcorão offline, em pt-BR/en/ar. Nada de código, layout, asset
ou string do Muslim Pro. O material antigo de engenharia reversa está em
`old/` e não é publicado.

## Stack

- Kotlin 2.1, AGP 8.10, Gradle 8.13 (wrapper no repo)
- Compose + Material 3, Navigation Compose
- Hilt, Room, DataStore, Media3 (entram nos milestones do plano)
- minSdk 26 · compileSdk/targetSdk 36 · pacote `com.farukzahra.muslimplus`

## Comandos

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Emulador de teste: `Pixel6` (`emulator-5554`).

## Estrutura

```
app/src/main/java/com/farukzahra/muslimplus/
  ui/{theme,nav,screens}   composição e navegação
  settings/                idioma e preferências
  prayer/ quran/ qibla/ location/ notifications/
app/src/main/res/values{,-pt-rBR,-ar}/strings.xml
app/src/main/res/xml/locales_config.xml
app/src/test/              testes JVM (paridade de string, locale, regras)
docs/superpowers/{specs,plans}/
```

## Regras

- Nenhuma string de UI no código. Toda string nova entra nos 3 locales;
  `LocaleParityTest` quebra o build se faltar.
- **Toda língua nova exige Alcorão na língua nova**: ao adicionar um locale em
  `AppLocale.supported`, rode `python tools/quran/build_quran_data.py` com o
  novo `TRANSLATIONS` (resource id do Quran.com) e gere
  `quran_translation_<tag>.txt`. O `QuranLocaleCoverageTest` quebra o build se
  o asset faltar ou não tiver 6.236 versos.
- Primeira execução segue o locale do aparelho; fallback `en`.
- RTL desde o primeiro layout (`start/end`, `supportsRtl`).
- Sem conta, ads, analytics ou backend.
- Nada de asset/string do Muslim Pro; o repo `old/` é só referência.
- Pacote `com.farukzahra.muslimplus` é permanente no Play.
- Segredos só em `../secrets/`; nunca no repo.
- Toda task termina com teste verde + commit.

## Workflow (mandatory skills)

| Fase | Skill / comando | Regra |
|---|---|---|
| Design | `brainstorming` | Sem código antes do spec aprovado em `docs/superpowers/specs/` |
| Plano | `writing-plans` | `docs/superpowers/plans/YYYY-MM-DD-*.md` |
| Arquitetura | `ask-before-architecture` | Perguntar antes de stack/estrutura |
| Build | TDD | Teste antes do código, commit por task |
| Debug | `systematic-debugging` | Causa raiz antes de corrigir |
| UI / bugs | `ui-change-e2e` | E2E em toda mudança de tela |
| Recorrência | `dont-forget` | Virar teste/CI, não lembrete em prosa |
| Automação | `automate-before-manual` | `../secrets/` e API antes de passo manual |
| Links | `validate-before-share` | HTTP 200 + conteúdo antes de mandar URL |
| Verificação | `verification-before-completion` | Evidência antes de dizer "pronto" |
| Ship | `/commit-push` | `semantic-version` → `caveman-commit` → push |
| Recap | `/recap` | HTML pedido × entregue em `docs/session-recaps/` |
| Handoff | `/handoff` | Documento compacto para a próxima sessão |

**Gates:** sem feature sem spec aprovado; sem "pronto" sem verificação;
todo idioma novo exige Alcorão no idioma novo (teste quebra o build).

## Idioma

Responder em português do Brasil.
