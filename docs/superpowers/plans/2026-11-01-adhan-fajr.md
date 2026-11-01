# Plano — adhan de Fajr, bandeiras e 22 vozes

Data: 01/11/2026. Versão: 1.3.0 (versionCode 8).
Spec: [`specs/2026-11-01-adhan-fajr-e-catalogo.md`](../specs/2026-11-01-adhan-fajr-e-catalogo.md).

## Tarefas

| # | Tarefa | Estado |
| --- | --- | --- |
| 1 | Converter 13 gravações novas para `res/raw` (mono 44,1 kHz, 48 kbps, loudnorm) | Feito |
| 2 | Copiar 9 bandeiras para `res/drawable-nodpi` e liberar PNG no `.gitignore` | Feito |
| 3 | `AdhanCountry` + `AdhanSound` com 22 vozes, país, `fajrOnly` e duração | Feito |
| 4 | `AdhanPolicy.defaultEnabled` com as cinco orações e `resolve` por Fajr | Feito |
| 5 | Preferências `adhan_fajr_sound`, `adhan_fajr_override` e aviso de migração | Feito |
| 6 | Migração pura (`AdhanMigrationPlan`) e chamada na abertura do app | Feito |
| 7 | `PrayerReceiver` resolve a voz da oração no disparo | Feito |
| 8 | Tela do adhan com cartões, bandeira, bottom sheet e prévia | Feito |
| 9 | Strings nos três idiomas (13 vozes, 9 países, tela e aviso) | Feito |
| 10 | Licenças: Adhan Notifications e bandeiras | Feito |
| 11 | Testes JVM: política, migração e catálogo | Feito |
| 12 | Testes instrumentados: catálogo, tela e escolha nas duas folhas | Feito |
| 13 | `test`, `lint`, `connectedDebugAndroidTest`, `install-local` | Feito |
| 14 | Versão 1.3.0, notas e publicação | Feito |
| 15 | Correção do card do rastreador em 360 dp e versão 1.3.1 | Feito |

## Evidências

- `gradlew test`: verde.
- `gradlew lint`: verde.
- `gradlew connectedDebugAndroidTest`: 33/33 verdes em `emulator-5554`
  (`MuslimPlusP0`) e 33/33 em `emulator-5556` (`Pixel6Test`).
- `scripts/install-local.ps1`: `OK emulator-5554` e `OK emulator-5556`,
  versionName 1.3.0.
- Tela conferida no Pixel 6: Fajr ligado, cartão padrão clássico, cartão Fajr
  Madinah com bandeira da Arábia Saudita; bottom sheet do Fajr com seis vozes e
  bandeiras; bottom sheet do dia com 16 vozes e bandeiras.
- APK debug: ~47 MB; áudio em `res/raw`: ~26,9 MB.
- `app-release.aab`: 35.287.257 bytes, versionCode 8, publicado nas trilhas
  interna e produção com notas em pt-BR, en e ar. Ver `docs/releases/1.3.0.md`.

## Notas de implementação

- A duração exibida é metadado do catálogo, não lida do arquivo em runtime.
- O bottom sheet usa `LazyColumn` com altura máxima de 480 dp.
- O aviso de migração só aparece quando a voz global antiga era uma gravação
  de Fajr.
- Quem já tem `adhan_prayers` salvo mantém o conjunto; o novo padrão com Fajr
  ligado vale para quem nunca mexeu.
- Nenhum asset, texto ou layout do Muslim Pro foi copiado.
