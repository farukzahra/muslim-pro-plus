# Muslim Plus — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development`
> (recommended) or `executing-plans` to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** App Android original (Kotlin + Compose) com horários de oração, Qibla e
Alcorão offline, em pt-BR/en/ar, publicável no Google Play.

**Architecture:** Módulo único `app`, pacotes por domínio, Compose + Material 3,
Hilt, Room, DataStore, Media3. Cálculo de oração local (`adhan-java`), dados do
Alcorão embarcados (Tanzil), áudio por stream com cache.

**Tech Stack:** Kotlin 2.x, AGP 8.x, Compose BOM, Hilt, Room, DataStore, Media3,
`adhan-java` (MIT), JUnit 5 + Compose UI Test, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-10-20-muslim-plus-design.md`

## Global Constraints

- Pacote `com.farukzahra.muslimplus`; minSdk 26; targetSdk/compileSdk 36.
- Locales: `en` (default), `pt-rBR`, `ar`. Zero string hardcoded.
- Sem conta, sem ads, sem analytics, sem backend.
- Offline-first: oração, texto do Alcorão e Qibla funcionam em modo avião.
- Nada de assets, layouts ou strings do Muslim Pro.
- Texto do Alcorão: Tanzil (CC BY 3.0) com atribuição visível.
- Keystore de upload só em `../secrets/`; nunca no repo.

## Review Focus

1. Troca de idioma no meio da sessão não pode perder estado nem reiniciar em loop.
2. Árabe RTL com números/referências de verso não pode embaralhar a ordem visual.
3. Virada de horário de verão e mudança de fuso não podem pular ou duplicar oração.
4. Alarme exato negado não pode falhar em silêncio: o usuário precisa ver o aviso.
5. Primeira execução sem rede precisa mostrar horários e Alcorão, não tela vazia.
6. Virada do dia hijri (maghrib × meia-noite) não pode mostrar data errada.

## File Structure

```
settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/farukzahra/muslimplus/
  MuslimPlusApp.kt, MainActivity.kt
  ui/theme/{Color,Type,Theme,Tokens}.kt
  ui/nav/{Destinations,AppNavHost}.kt
  ui/screens/{today,prayers,quran,qibla,more,settings}/
  prayer/{PrayerEngine,PrayerMethod,PrayerSchedule,PrayerTime}.kt
  location/{LocationRepository,CityCatalog,City}.kt
  quran/{QuranRepository,QuranSearch,QuranAudio,QuranAssets}.kt
  qibla/{QiblaRepository,QiblaMath,SensorSource}.kt
  settings/{SettingsRepository,LanguageRepository,AppLocale}.kt
  notifications/{PrayerScheduler,PrayerReceiver,BootReceiver}.kt
app/src/main/res/values{,-pt-rBR,-ar}/strings.xml
app/src/main/res/xml/locales_config.xml
app/src/test/java/... (JVM)
app/src/androidTest/java/... (instrumented)
tools/quran/build_quran_data.py
tools/cities/build_cities.py
```

## M0 — Fundação (app abre com 5 abas e 3 idiomas)

### Task 1: Projeto Gradle + shell Compose

**Files:** `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`,
`app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `MuslimPlusApp.kt`,
`MainActivity.kt`, `ui/nav/*`, `ui/theme/*`.

**Interfaces:**
- Produces: `enum class Tab { TODAY, PRAYERS, QURAN, QIBLA, MORE }`;
  `@Composable fun MuslimPlusApp()`; `AppNavHost(navController)`.

- [ ] Step 1: teste JVM `TabTest` — 5 abas, ids estáveis `today/prayers/quran/qibla/more`.
- [ ] Step 2: rodar `./gradlew test` e ver falhar.
- [ ] Step 3: criar projeto Gradle, `MainActivity` com `Scaffold` + `NavigationBar` e
  placeholders; tema com tokens (cor, tipo, espaço) e dark/light.
- [ ] Step 4: `./gradlew test` e `./gradlew assembleDebug` verdes.
- [ ] Step 5: commit `feat: android shell with five tabs`.

### Task 2: i18n de base + paridade

**Files:** `res/values*/strings.xml`, `res/xml/locales_config.xml`,
`settings/LanguageRepository.kt`, `settings/AppLocale.kt`, `test/.../LocaleParityTest.kt`.

**Interfaces:**
- Produces: `data class AppLocale(val tag: String?)` (`null` = sistema);
  `suspend fun LanguageRepository.set(tag: String?)`; `fun resolveLocale(requested: String?, system: String): String`.
- Regra: `requested` válido → ele; senão locale do sistema se suportado; senão `en`.

- [ ] Step 1: `LocaleParityTest` — mesmas chaves em en/pt/ar, nenhum valor vazio,
  `plurals` consistentes.
- [ ] Step 2: rodar e ver falhar (locales inexistentes).
- [ ] Step 3: criar os 3 `strings.xml` + `locales_config.xml` + repositório DataStore +
  aplicação via `AppCompatDelegate.setApplicationLocales`.
- [ ] Step 4: teste verde; `./gradlew lint` sem `HardcodedText`.
- [ ] Step 5: commit `feat(i18n): add en/pt-BR/ar with device default`.

### Task 3: Seletor de idioma

**Files:** `ui/screens/settings/LanguageScreen.kt`, teste instrumentado.

**Interfaces:** consome `LanguageRepository` e `resolveLocale` da Task 2.

- [ ] Step 1: teste Compose — opções "Sistema (padrão)", English, Português, العربية;
  escolher العربية recria a activity e mantém a seleção.
- [ ] Step 2: rodar e ver falhar.
- [ ] Step 3: implementar tela e persistência.
- [ ] Step 4: teste verde.
- [ ] Step 5: commit `feat(i18n): language picker`.

## M1 — Oração

### Task 4: Motor de cálculo

**Files:** `prayer/PrayerEngine.kt`, `PrayerMethod.kt`, `PrayerSchedule.kt`, teste.

**Interfaces:** `fun schedule(date: LocalDate, lat: Double, lon: Double, tz: ZoneId, method: PrayerMethod, offsets: Map<Prayer, Int>): PrayerSchedule`;
`PrayerSchedule(next: PrayerTime, times: Map<Prayer, Instant>)`.

- [ ] Step 1: teste com coordenadas/datas de referência (Makkah, São Paulo, Londres no
  verão e no inverno) comparando com valores publicados, tolerância ±1 min.
- [ ] Step 2: falhar.
- [ ] Step 3: wrapper do `adhan-java`, métodos + `HighLatitudeRule` + offsets.
- [ ] Step 4: verde, incluindo DST e |lat| > 48°.
- [ ] Step 5: commit `feat(prayer): local calculation engine`.

### Task 5: Localização e catálogo de cidades

**Files:** `location/*`, `tools/cities/build_cities.py`, assets `cities.json`.

**Interfaces:** `suspend fun LocationRepository.current(): Coordinates?`;
`fun CityCatalog.search(q: String, locale: String): List<City>`; `data class City(name, country, lat, lon, zoneId)`.

- [ ] Step 1: teste — São Paulo resolve `America/Sao_Paulo`; busca "meca" acha Makkah;
  cidade sem GPS produz horários no fuso da cidade.
- [ ] Step 2: falhar.
- [ ] Step 3: gerar subset GeoNames (cities5000) + FusedLocation com fallback.
- [ ] Step 4: verde.
- [ ] Step 5: commit `feat(location): city catalog and device location`.

### Task 6: Tela Hoje

- [ ] Step 1: teste Compose — mostra próxima oração, contador e lista do dia.
- [ ] Step 2: falhar. Step 3: implementar. Step 4: verde.
- [ ] Step 5: commit `feat(ui): today screen`.

### Task 7: Tabela do mês + ajustes

- [ ] Step 1: teste Compose — 30/31 dias, destaque de hoje, offsets persistem.
- [ ] Step 2-4: TDD. Step 5: commit.

### Task 8: Notificações

**Files:** `notifications/*`, teste com `Clock` fake.

**Interfaces:** `fun PrayerScheduler.scheduleNext(schedule, enabled: Set<Prayer>)`;
`fun canScheduleExact(context): Boolean`.

- [ ] Step 1: teste — agenda 7 dias, reagenda em boot/fuso, sem permissão cai para
  inexato e marca `needsExactPermission`.
- [ ] Step 2: falhar. Step 3: implementar + receivers + permissões no onboarding.
- [ ] Step 4: verde + teste instrumentado de notificação.
- [ ] Step 5: commit `feat(notifications): exact alarms with fallback`.

## M2 — Qibla

### Task 9: Matemática e sensores

**Files:** `qibla/QiblaMath.kt`, `QiblaRepository.kt`, teste.

- [ ] Step 1: teste — bearing para a Caaba de São Paulo ≈ 67°, de Londres ≈ 119°;
  declinação aplicada; sem magnetômetro retorna `Unavailable`.
- [ ] Step 2-4: TDD. Step 5: commit.

### Task 10: Tela Qibla

- [ ] Step 1: teste Compose — mostrando graus, distância e estado de calibração.
- [ ] Step 2-4: TDD. Step 5: commit.

## M3 — Alcorão

### Task 11: Pipeline de dados

**Files:** `tools/quran/build_quran_data.py`, `app/src/main/assets/quran.json`.

- [ ] Step 1: teste de integridade — 114 suras, 6.236 versos, chaves únicas
  `sura:ayah`, texto não vazio, checksum do arquivo fixado.
- [ ] Step 2: falhar. Step 3: baixar Tanzil (uthmani), gerar JSON + metadados.
- [ ] Step 4: verde. Step 5: commit.

### Task 12: Leitor e persistência

- [ ] Step 1: teste — abrir sura 2 mostra 286 versos; última leitura persiste após
  recriar o app; bookmark alterna.
- [ ] Step 2-4: TDD (Room + Compose). Step 5: commit.

### Task 13: Busca

- [ ] Step 1: teste — "الرحمن" acha versos sem harakat; busca por "cow" acha Al-Baqarah
  2:67–71; busca vazia não quebra.
- [ ] Step 2-4: TDD (FTS + normalização). Step 5: commit.

### Task 14: Áudio

- [ ] Step 1: teste — download de sura grava arquivo e índice; cache LRU remove o mais
  antigo; sem rede mostra erro e mantém texto.
- [ ] Step 2-4: TDD (Media3 + downloader com HTTP fake). Step 5: commit.

## M4 — Mais, loja e privacidade

### Task 15: Telas do Mais

- [ ] Settings (método, offsets, notificações), Sobre, Licenças (Tanzil, GeoNames,
  Amiri, adhan-java), Privacidade (link para a página publicada).
- [ ] Teste Compose de navegação dos 6 itens. Commit.

### Task 16: Release

- [ ] `versionCode/versionName`, assinatura via `../secrets/`, `bundleRelease`,
  assets de loja (ícone 512, feature graphic, 4 screenshots × 3 idiomas).

## M5 — Qualidade

### Task 17: E2E e CI

- [ ] Script adb (5 abas + troca de idioma + modo avião) e workflow
  `test + lint + assembleDebug`; upload manual na trilha interna.

## Critérios de conclusão

- `./gradlew test lint assembleDebug` verdes.
- Testes de paridade e de RTL verdes.
- APK instalado no `Pixel6` com 5 abas funcionando nos 3 idiomas.
- `bundleRelease` assinado, pronto para a trilha interna.
