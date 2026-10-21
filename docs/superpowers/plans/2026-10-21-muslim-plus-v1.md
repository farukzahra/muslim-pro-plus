# Muslim Plus — Plano v1 (completo, sem beta)

> Substitui o plano de 2026-10-20. Escopo: **versão completa para produção**,
> não MVP. Toda feature listada entra antes do primeiro upload público.

## Definição de pronto

- [ ] 5 abas completas, sem tela placeholder.
- [ ] Onboarding na primeira execução (idioma, localização, método, notificações).
- [ ] Horários corretos com GPS **e** cidade manual, offsets e fuso.
- [ ] Notificações com alarme exato, fallback visível e reagendamento em boot/fuso/hora.
- [ ] Alcorão com árabe, transliteração, pt-BR e inglês; busca, marcadores, última leitura e áudio.
- [ ] Qibla com sensor, declinação e calibração.
- [ ] Mais com configurações reais, licenças, privacidade e versão do build.
- [ ] Três idiomas completos (en, pt-BR, ar), RTL sem corte, paridade testada.
- [ ] Testes: unidade, Compose instrumentado e E2E adb verdes.
- [ ] CI rodando `test lint assembleDebug`.
- [ ] AAB assinado, ícone adaptativo, listing, data safety e política pública.
- [ ] Trilha interna aprovada no Play.

## Fases

### F1 — Fundação técnica

- Room + KSP (marcadores, última leitura, FTS do texto e das traduções).
- Migrar `ServiceLocator` para Hilt **somente** se o custo for baixo; caso
  contrário, manter e documentar. (Decisão do construtor: manter ServiceLocator
  na v1 para não reescrever 20 arquivos sem ganho de produto.)
- `versionName`/`versionCode` vindos do Gradle (`BuildConfig`), não hardcoded.
- Workflow GitHub Actions: `test`, `lint`, `assembleDebug`.

Verificação: `.\gradlew.bat test lint assembleDebug` e CI verde.

### F2 — Oração completa

- Offsets por oração persistidos (tela em Configurações).
- Offset hijri ±1 dia.
- `TIMEZONE_CHANGED`, `TIME_SET`, `MY_PACKAGE_REPLACED` no reagendamento.
- Aviso na UI quando o alarme exato está negado.
- Verso do dia na aba Hoje (lista curada embarcada, rotação por data).
- Formatação sem concatenação de string (usar recurso com `%1$s · %2$s`).

Verificação: testes de `NextPrayerResolver` com relógio fixo + `dumpsys alarm`
mostrando o próximo alarme após mudar fuso.

### F3 — Localização e onboarding

- `tools/cities/build_cities.py` gerando catálogo GeoNames `cities5000`
  (lat/lon + fuso IANA), com atribuição CC BY 4.0.
- `LocationRepository` com `LocationManager` (GPS/rede) + fallback manual.
- Busca de cidade na tela Orações e no onboarding.
- Onboarding pulável em 4 passos, idioma pré-preenchido pelo sistema.

Verificação: emulador com `adb emu geo fix`, troca de cidade reflete em Hoje e
Orações; modo avião mantém horários.

### F4 — Alcorão completo

- Última leitura e marcadores em Room, restaurando posição ao reabrir.
- Tela de busca de versos (FTS + normalização árabe já existente).
- Áudio por verso com Media3: stream + download por sura, cache LRU,
  `MediaSessionService` para tocar com tela apagada.
- Fonte Amiri Quran (OFL) no leitor + entrada em Licenças.
- Metadados de sura: significado em pt-BR (revisão do usuário).

Verificação: teste de download com HTTP fake; tocar uma sura e girar a tela;
reabrir o app e cair no último verso.

### F5 — Mais e configurações

- Grade com 6 itens: Configurações, Idioma, Notificações, Sobre, Licenças,
  Privacidade.
- **Exibição do Alcorão no Mais**: escolher camadas (árabe, transliteração,
  pt-BR, inglês), pelo menos uma, mesma preferência usada no leitor.
- **Tema**: escuro como padrão, claro disponível nas configurações.
- Configurações: método, offsets, cidade, hijri, notificações.
- Licenças: Tanzil (CC BY 3.0), GeoNames (CC BY 4.0), Amiri (OFL), adhan-java
  (MIT), traduções e áudio com atribuição.
- Privacidade: link para a página publicada.
- Versão lida do `BuildConfig`.

Verificação: teste Compose navegando nos 6 itens; dump sem string hardcoded.

### F6 — Marca e release

- Ícone adaptativo + mipmap + 512×512 para o Play.
- Feature graphic 1024×500 e 4 screenshots por idioma.
- Keystore de upload em `../secrets/android/` (nunca no repo).
- `signingConfig` de release, `bundleRelease`, `apksigner verify`.
- Listing pt-BR/en, data safety, classificação, política pública no ar.
- Upload na trilha interna.

Verificação: AAB assinado + revisão da trilha interna aprovada.

### F7 — Qualidade

- `androidTest` Compose: 5 abas, troca de idioma, leitor, diálogo de camadas.
- E2E adb: `tests/verify_language.py` + novo `tests/e2e_app.py` (5 abas,
  modo avião, notificação).
- CI com os dois níveis.

Verificação: `connectedDebugAndroidTest` e E2E verdes no `Pixel6`.

## Decisões

| Decisão | Escolha |
|---|---|
| Áudio | **Entra na v1**: stream do CDN + download por sura, fonte abstraída atrás de `QuranAudio`; verificação de termos é gate de release |
| Repositório | `old/` sai do repo público antes do Play (repo privado separado) |
| DNS | `muslimplus.faruk.dev.br` criado antes do upload |
| Hilt | Não entra na v1; `ServiceLocator` documentado |
| Tema | Escuro por padrão; claro disponível nas configurações |
| Room | Entra (marcadores, última leitura, FTS) |
| GPS | `LocationManager` nativo, sem Play Services |

## Gates de release

1. Termos do áudio verificados e atribuição visível.
2. Traduções (Helmi Nasr / Pickthall) com atribuição e termos conferidos.
3. Política de privacidade respondendo 200.
4. Revisão nativa do árabe.
5. `./gradlew test lint assembleDebug` e `connectedDebugAndroidTest` verdes.

## Riscos

- Alarme exato negado no Android 14+ → fallback inexato com aviso.
- Fonte/tradução árabe sem revisão nativa → adiar `ar` no listing, não no app.
- Catálogo GeoNames grande → subset por população para manter o APK abaixo de 20 MB.
- Áudio: se os termos não fecharem, a feature fica atrás de flag e o release
  segue sem ela (sem refazer o player).
