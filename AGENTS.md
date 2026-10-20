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
- Primeira execução segue o locale do aparelho; fallback `en`.
- RTL desde o primeiro layout (`start/end`, `supportsRtl`).
- Sem conta, ads, analytics ou backend.
- Nada de asset/string do Muslim Pro; o repo `old/` é só referência.
- Pacote `com.farukzahra.muslimplus` é permanente no Play.
- Segredos só em `../secrets/`; nunca no repo.
- Toda task termina com teste verde + commit.

## Workflow

1. Spec aprovado em `docs/superpowers/specs/`.
2. Plano em `docs/superpowers/plans/`.
3. TDD por task; commit a cada task.
4. Verificação com evidência (teste, build, dump/screenshot no emulador).

## Idioma

Responder em português do Brasil.
