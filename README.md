# Muslim Plus

App islâmico original para Android — horários de oração, Qibla e Alcorão.
Kotlin + Jetpack Compose, offline-first, em pt-BR, inglês e árabe.

## Status

- **M0 concluído**: projeto Gradle, shell com 5 abas, i18n en/pt-BR/ar com
  teste de paridade de strings e locale do sistema.
- Próximo: seletor de idioma (M0), depois motor de oração (M1).

## Rodar

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat test
.\gradlew.bat installDebug
```

Testes JVM cobrem rotas das abas, resolução de locale e paridade das strings.

## Estrutura

```
app/          módulo Android (Compose, Material 3)
docs/         spec e plano em docs/superpowers/
old/          projeto antigo de patch do Muslim Pro (arquivado, não publicado)
tools/        scripts dos próximos milestones (dados do Alcorão, cidades)
```

## Documentos

- Spec: `docs/superpowers/specs/2026-10-20-muslim-plus-design.md`
- Plano: `docs/superpowers/plans/2026-10-20-muslim-plus.md`

## Legado

O trabalho de engenharia reversa do Muslim Pro 12.6.10 está em
[`old/`](old/README.md). Não faz parte deste app.
