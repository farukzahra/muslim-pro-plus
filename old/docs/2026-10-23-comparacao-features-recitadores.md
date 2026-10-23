# Muslim Pro 12.6.10 × Muslim Plus — levantamento

Data: 23/10/2026. Pesquisa, sem implementação de funcionalidades.

## Escopo e evidências

O pacote `com.bitsmedia.android.muslimpro` foi aberto no Pixel6,
`emulator-5554`. A instalação arquivada tem modificações descritas em
`old/README.md`; não representa necessariamente a experiência comercial
original ou a versão atual da loja.

Evidência direta desta sessão: `old/evidence/ui-comparison-{live,quran,settings,reciters}.xml`.
Evidência histórica: `old/evidence/e2e-report.json`. Seu PASS comprova abertura
de tela sem crash naquele teste, não funcionamento completo de serviços online.
Não houve captura de rede nem nova sessão REA: as ferramentas REA não estavam
disponíveis; foram usados ADB, recursos e código já decompilado.

O inventário do Plus usa o código atual, especialmente `MuslimPlusApp.kt`,
`MoreScreen.kt`, `QuranDisplayScreen.kt`, `QuranListScreen.kt`,
`QuranReaderScreen.kt` e os arquivos em `quran/audio/`.

## Diferenças no Alcorão

| Recurso observado no Pro | Plus atual |
| --- | --- |
| Selecionar recitador e baixar suas recitações | Apenas Mishary Alafasy, EveryAyah 128 kbps |
| Narração de traduções | Traduções em texto; sem seletor de narrador |
| Navegação por Juz | Lista por sura e busca; sem aba Juz |
| Listas de reprodução | Fila interna da sura; sem playlists do usuário |
| Planejador Khatam | Sem metas/plano de conclusão |
| Repetir cada ayah | Sem ajuste de repetição |
| Reproduzir próxima sura ao terminar | Fila montada por sura; sem preferência equivalente |
| Rolagem automática e manter tela ligada como preferências | Sem controles equivalentes |
| Tajwid e escolha de texto árabe (IndoPak mostrado) | Sem seletor equivalente ou Tajwid colorido |

A tela de recitações exibiu Abdul Rahman As Sudais, Ali al Hudhaify,
Saad Al-Ghamdi, Sheikh Bander Baleelah, Abdul Basit Abdus Samad,
Mishari Rashid al-Afasy, Teuku Wisnu (tradução Kemenag) e Ibrahim Walk
(Saheeh International). A interface informa download integral para Premium.
Não foi testado download nem reprodução de cada opção.

No Plus, `QuranAudioSource.kt` fixa `EveryAyahSource` e
`Alafasy_128kbps`; `QuranAudioItems.kt` também fixa o nome exibido.
`QuranAudioStore.kt` usa `audio/{sura}/{verso}.mp3`, sem recitador no
caminho. Uma integração futura precisa separar o cache por provedor e
recitação para impedir que uma voz selecionada toque o arquivo de outra.

Já existem no Plus: busca, traduções, transliteração, favoritos, continuar
leitura, áudio por verso e download por sura. Não são funcionalidades ausentes.

## Outros módulos

O relatório histórico demonstra abertura das telas abaixo; o Plus não tem
rotas equivalentes:

- Rastreador, Tasbih/dhikr e Duas/súplicas.
- Zakat, Shahadah e Nomes (há referência estática a áudio `names99`).
- Locais halal e mesquitas próximas.
- Haje/Umrah e Hajj Journey.
- Mensagens de saudação e feriados.
- Loja e comunidade, que não correspondem ao escopo atual sem conta/backend.

Parciais: o Plus mostra data hijri e permite ajuste, mas não tem calendário
completo; tem verso diário, mas não um módulo dedicado de inspiração.

O relatório também registra Makkah, porém seu destino é SuraActivity: não há
evidência suficiente para chamar isso de transmissão ao vivo. Ajuda e Voos
ficaram PARTIAL e não entram como recursos funcionais confirmados.

## Fontes públicas para recitações

As contagens abaixo são o retorno observado nesta data, não garantia de
cobertura integral nem número de pessoas distintas.

| Fonte | Oferta | Adequação ao Plus |
| --- | --- | --- |
| EveryAyah | Catálogo público com 79 entradas numéricas, incluindo variantes de qualidade/estilo; arquivos por ayah | Menor mudança: já é a origem atual; catálogo sem autenticação no teste |
| Al Quran Cloud | REST por edição/sura/ayah; consulta de áudio árabe retornou 176 edições | Compatível com áudio por verso; API e OpenAPI acessíveis sem credenciais no teste |
| MP3Quran v3 | Consulta retornou 243 recitadores, com moshaf, riwaya, servidor e suras disponíveis | Boa opção de catálogo e áudio por sura; não presumir que cada entrada tenha as 114 suras ou alinhamento por ayah |
| Quran.com / Quran Foundation | Recursos de recitações por ayah e por capítulo | Documentação atual exige OAuth2 Client Credentials no backend para Content API; não embutir segredo no APK |

Endpoints validados com HTTP 200 e conteúdo esperado:

- `https://everyayah.com/data/recitations.js` (JSON servido como JavaScript).
- `https://api.alquran.cloud/v1/edition?format=audio&language=ar`.
- `https://api.alquran.cloud/v1/documentation/openapi/yaml` (especificação OpenAPI).
- `https://www.mp3quran.net/api/v3/reciters?language=eng`.

O catálogo MP3Quran distingue a língua dos nomes/metadados da língua da
recitação. Também é necessário distinguir recitador, riwaya, estilo e bitrate;
duas entradas da mesma pessoa não são necessariamente o mesmo áudio.

Fontes primárias consultadas:

- [EveryAyah](https://everyayah.com/).
- [Al Quran Cloud API](https://alquran.cloud/api).
- [Al Quran Cloud CDN](https://alquran.cloud/cdn).
- [MP3Quran API](https://www.mp3quran.net/ar/api).
- [Quran Foundation: recitações](https://api-docs.quran.com/docs/content_apis_versioned/4.0.0/recitations/).
- [Quran Foundation: autenticação](https://api-docs.quran.com/docs/quickstart/).
- [Muslim Pro: seleção de recitadores](https://helpdesk.muslimpro.com/help/en/articles/how-to-choose-a-different-reciter-for-the-quran).

Recomendação técnica, ainda sem decisão de arquitetura: começar pelo catálogo
EveryAyah, pois mantém o fluxo por verso existente e dispensa backend.
Al Quran Cloud é outra opção direta. A disponibilidade pública não resolve
sozinha direitos de redistribuição/download: confirmar condições aplicáveis
às gravações escolhidas antes de disponibilizá-las no app.

Não foi demonstrado que o Muslim Pro use Quran.com, EveryAyah ou Al Quran
Cloud. O decompilado contém referência a
`https://download.muslimpro.com/quran-audio` em `o/FacebookSDKJSInterface.java`;
isso é evidência estática de um endereço próprio, não prova de origem/licença
das gravações nem de tráfego atual. A integração proposta usa provedores
públicos independentes.

## Verificação

- Navegação real até Al Qur'an → Ajustes → Recitações no Pixel6.
- Dumps XML confirmam opções e nomes acima.
- Consultas públicas retornaram HTTP 200 e catálogos interpretáveis.
- `gradlew.bat test`: BUILD SUCCESSFUL, tarefas de testes UP-TO-DATE.
- Nenhum código, layout, asset ou string do legado foi incorporado ao Plus.
