# Quran Audio and Storage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Selecionar voz árabe, reutilizar áudio ouvido e gerenciar downloads por voz/sura ou Alcorão completo.

**Architecture:** Manter Kotlin/Compose/DataStore/Media3. Catálogo EveryAyah curado com IDs estáveis; downloads em filesDir separados por voz, cache Media3 em cacheDir com LRU. Um coordenador no processo serializa downloads, publica progresso e permite retomar arquivos completos.

**Tech Stack:** Kotlin 2.1, Compose Material 3, coroutines, DataStore, Media3 1.8.1, JUnit/Compose Android tests.

**Spec:** docs/superpowers/specs/2026-10-23-quran-reciter-design.md (aprovado e ampliado em 23/10).

## Global Constraints

- Áudio sempre árabe. Sem backend/conta/analytics. Sem envio ao Play.
- Strings en/pt-BR/ar, RTL; preferência persistida e arquivos identificados por voz.
- Cache automático 300 MiB; downloads explícitos não sofrem LRU.
- Não começar outros itens do TODO sem perguntar ao usuário.

## Review Focus

- Troca da voz durante fila/download não mistura áudio/metadados.
- Migração do diretório legado preserva Alafasy e não atribui a outras vozes.
- Cancelamento/falha preserva versos completos e nunca promove arquivo parcial.
- Limpeza de cache não apaga downloads; exclusão concorrente com download é bloqueada.
- Offline, disco cheio e processo reiniciado não geram indicador falso de sura completa.

## Task 1 — Catálogo e armazenamento protegido

**Files:** quran/audio/QuranReciters.kt, QuranAudioStore.kt, QuranAudioDownloader.kt; testes JVM correspondentes.

**Interfaces:** QuranReciter implements QuranAudioSource com id, reciter e bitrateKbps. QuranReciters.all/default/find(id). QuranAudioStore(root,source,limitBytes) mantém API existente, adiciona savedVerses(surah,count), deleteSurah(surah), deleteAll(); root inclui identidade para fontes novas, migra legado Alafasy. Downloads explícitos não são cache evictável.

- [x] Escrever testes para isolamento entre vozes, legado, integridade parcial, exclusão e downloads protegidos; executar e observar falha.
- [x] Implementar catálogo verificado e storage mantendo default/URLs antigos compatíveis.
- [x] Testar download retoma e falha limpa .part; rodar testes JVM e commitar task.

## Task 2 — Cache real de reprodução e preferência

**Files:** QuranPlaybackService.kt, QuranAudioItems.kt, QuranAudioController.kt, SettingsRepository.kt; novo QuranStreamingCache.kt; testes de preferência/Media3 Android.

- [x] Adicionar testes de identidade/seleção e cache repetido sem rede, observar falha.
- [x] Persistir quranReciterId; usar artista/URL da voz escolhida; pausar seleção alterada.
- [x] Media3 CacheDataSource com SimpleCache e LRU de 300 MiB; limpeza remove somente esse cache.
- [x] Propagar erro recuperável ao leitor; manter suporte a arquivos offline; verificar e commitar.

## Task 3 — Coordenador e tela de áudio

**Files:** novo QuranAudioLibrary.kt, AudioStorageScreen.kt; SettingsScreen.kt, MuslimPlusApp.kt, QuranReaderViewModel.kt e QuranReaderScreen.kt; strings nos 3 locales; teste Compose.

**Interfaces:** coordenador application-scoped com StateFlow de progresso (reciterId, surah, done,total,failed,running), download(reciter,surahs), cancel(), inventário e ações de exclusão.

- [x] Escrever teste Compose para abrir configurações/selecionar voz/limpar cache sem apagar offline; observar falha.
- [x] Coordenar um download por vez, independente da navegação, captura voz, cancelamento e retomada manual; conferir espaço e mostrar estimativa antes de download completo.
- [x] Tela com seleção/busca, filtro vozes baixadas, inventário 114 suras, completas/parciais, tamanho e confirmações; excluir bloqueado durante download; limpar cache preserva downloads.
- [x] Integrar leitor e configuração; nome/status refletem voz atual; testes e commit.

## Task 4 — Verificação e revisão final

- [x] Rodar gradlew test lint assembleDebug e teste Android instrumentado no Pixel6.
- [x] Verificar UI pt-BR/ar, cache em segundo play offline, arquivos por voz, exclusão sem downloads reais grandes.
- [x] Revisão independente do diff; resolver achados, atualizar TODO/spec e evidências.
- [x] Commit local; instalar/abrir debug no Pixel6. Sem bundle/upload/publicação/push automático.

## Google Play — tarefa independente, somente consulta

- [x] Consultar tracks/versionCode e revisão no Console se autenticação disponível.
- [x] Diferenciar completed da track de aprovação; reportar limitações reais. Não executar play_release.py.

## Evidências — 23/10/2026

- `gradlew test` ok; `lint` 0 erros; `assembleDebug` ok.
- `connectedDebugAndroidTest` no Pixel6 (`emulator-5554`): 12 testes, 0 falhas — inclui
  `AudioStorageScreenTest`, `QuranAudioLibraryTest`, `QuranStreamingCacheTest`,
  `QuranAudioItemsTest` e `QuranReaderAudioTest`.
- Manual pt-BR e árabe/RTL no Pixel6: seleção de voz, filtro, inventário por sura,
  estimativa do Alcorão completo, limpeza de cache preservando downloads, nome da voz
  no leitor e atalho para Áudio no diálogo de exibição.
- Revisão independente do diff com achados corrigidos: fila resolve o arquivo local a
  cada abertura sem duplicar download no cache; troca de voz pausa e remonta a fila na
  voz nova; download bloqueado quando já existe fila; cancelamento aborta a conexão
  HTTP; `saved` não varre a sura a cada verso; voz desconhecida oferece ação; `SimpleCache`
  criado fora da main thread; string `quran_reciter_alafasy` morta removida.
- Play (somente leitura, via service account): produção `1.0.0` / versionCode `1` com
  status `completed`; teste interno idem; beta/alpha vazios; ficha pública BR responde
  404; 0 reviews. `completed` não comprova aprovação. Nenhum upload ou publicação.
