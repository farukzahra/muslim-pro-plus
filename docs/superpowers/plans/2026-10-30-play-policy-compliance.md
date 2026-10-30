# Plano: conformidade Play out/2026 (localização aproximada + R8)

Spec: [2026-10-30-play-policy-2026-10.md](../specs/2026-10-30-play-policy-2026-10.md)
Data: 30/10/2026

## Tarefa 1: localização aproximada apenas

1. `app/src/test/.../LocationScopeTest.kt` primeiro, com falha esperada:
   - manifesto declara `ACCESS_COARSE_LOCATION`;
   - manifesto não declara `ACCESS_FINE_LOCATION`;
   - manifesto não declara permissões restritas fora do escopo
     (`READ_CALL_LOG`, `READ_CONTACTS`, `MANAGE_EXTERNAL_STORAGE`,
     `WRITE_CONTACTS`, `READ_SMS`);
   - `LocationRepository.PERMISSIONS` tem exatamente uma entrada.
2. `AndroidManifest.xml`: remover `ACCESS_FINE_LOCATION` e ajustar o comentário.
3. `LocationRepository`: lista só com `ACCESS_COARSE_LOCATION` e KDoc
   explicando o escopo mínimo.
4. Rodar `.\gradlew.bat test`.

## Tarefa 2: release com R8

1. `app/build.gradle.kts`: `isMinifyEnabled = true`, `isShrinkResources = true`.
2. `proguard-rules.pro`: `-keepattributes SourceFile,LineNumberTable` e
   `-renamesourcefileattribute SourceFile`.
3. `.\gradlew.bat bundleRelease assembleRelease` com keystore do `local.properties`.
4. Medir DEX antes/depois (soma de `base/dex/classes*.dex` no AAB) e conferir
   que não há classe de app sem ofuscação.
5. Corrigir keep rules se o build ou o app quebrar.

## Tarefa 3: verificação no aparelho

1. Emulador `Pixel6` ligado.
2. Instalar o APK de release assinado, abrir e percorrer: onboarding (cidade
   manual e botão de localização), Hoje, Orações, Alcorão com áudio, Qibla,
   idioma ar (RTL), Mais.
3. `adb logcat` sem `FATAL EXCEPTION` e `dumpsys package` com as permissões
   esperadas.
4. `.\scripts\install-local.ps1` no fim, com `OK <serial>`.

## Tarefa 4: documentação e commit

1. `docs/store/checklist.md`: permissão única, DEX antes/depois, versão.
2. `docs/superpowers/TODO.md`: registrar a decisão e a pendência de conta
   (status de comerciante).
3. `versionCode` 6 e `versionName` 1.1.4.
4. Commit único no formato convencional.
